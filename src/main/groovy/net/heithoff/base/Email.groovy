package net.heithoff.base

import groovy.util.logging.Slf4j
import net.heithoff.App
import net.heithoff.WorkdayClientService
import workday.com.bsvc.BusinessProcessParametersType
import workday.com.bsvc.CommunicationMethodUsageInformationDataType
import workday.com.bsvc.CommunicationUsageTypeDataType
import workday.com.bsvc.CommunicationUsageTypeObjectIDType
import workday.com.bsvc.CommunicationUsageTypeObjectType
import workday.com.bsvc.ContactInformationDataType
import workday.com.bsvc.ContactInformationForPersonEventDataType
import workday.com.bsvc.EmailAddressInformationDataType
import workday.com.bsvc.MaintainContactInformationForPersonEventRequestType
import workday.com.bsvc.MaintainContactInformationForPersonEventResponseType
import workday.com.bsvc.WorkerObjectType
import workday.com.bsvc.human_resources.HumanResourcesPort

@Slf4j
class Email {
    String parentWid
    String address
    String comment
    String usageType
    String usageNamedId
    String usageWid
    String comments
    Boolean isPublic
    Boolean isPrimary
    Boolean dirty
    Boolean addOnly

    Email() {
        addOnly = true
    }

    Email(String parentWid, EmailAddressInformationDataType emailAddressData) {
        this.parentWid = parentWid
        addOnly = false
        if(emailAddressData.usageData.size() > 1) {
            log.warn("This library assumes their is only one usageData per email, Open an issue in github if this assumption is wrong, https://github.com/HybridProgrammer/WorkdayWSClient")
        }
        if(emailAddressData.usageData.first().typeData.size() > 1) {
            log.warn("This library assumes their is only one typeData per email, Open an issue in github if this assumption is wrong, https://github.com/HybridProgrammer/WorkdayWSClient")
        }

        CommunicationMethodUsageInformationDataType communicationMethodUsageInformation = emailAddressData.usageData.first()
        CommunicationUsageTypeDataType usageTypeData = communicationMethodUsageInformation.typeData.first()
        this.address = emailAddressData.emailAddress
        this.isPublic = communicationMethodUsageInformation.public
        this.isPrimary = usageTypeData.primary
        this.comments = communicationMethodUsageInformation.comments
        usageTypeData.typeReference.ID.each { CommunicationUsageTypeObjectIDType id ->
            usageNamedId = App.properties().get("email.communication.usage.type.id", "Communication_Usage_Type_ID").toString()
            if(usageNamedId.equalsIgnoreCase(id.type)) {
                usageType = id.value
            }

            if("WID".equalsIgnoreCase(id.type)) {
                usageWid = id.value
            }
        }

        resetDirty()
    }

    boolean save() {
        WorkdayClientService workdayClientService = WorkdayClientService.workdayClientService
        MaintainContactInformationForPersonEventRequestType request = new MaintainContactInformationForPersonEventRequestType()
        request.version = workdayClientService.version
        request.addOnly = this.addOnly
        request.businessProcessParameters = new BusinessProcessParametersType()
        request.businessProcessParameters.autoComplete = true
        request.businessProcessParameters.runNow = true
        request.maintainContactInformationData = new ContactInformationForPersonEventDataType()
        request.maintainContactInformationData.workerReference = wrapWorkerObjectType(workdayClientService)
        request.maintainContactInformationData.workerContactInformationData = new ContactInformationDataType()
        request.maintainContactInformationData.workerContactInformationData.emailAddressData.add(wrapEmailInformation())
        request.maintainContactInformationData.effectiveDate = workdayClientService.generateEffectiveDate()

        def resources = workdayClientService.getResources("Human_Resources")
        try {
            MaintainContactInformationForPersonEventResponseType response = ((HumanResourcesPort) resources["port"]).maintainContactInformation(request)
        } catch(Exception e) {
            log.error(e.message)
            throw e
        }

        log.debug("saved email: " + this.address)
        return true

    }

    private EmailAddressInformationDataType wrapEmailInformation() {
        EmailAddressInformationDataType emailAddressInfo = new EmailAddressInformationDataType(emailAddress: this.address)
        CommunicationMethodUsageInformationDataType commMethod = new CommunicationMethodUsageInformationDataType(public: this.isPublic)
        emailAddressInfo.usageData.add(commMethod)
        CommunicationUsageTypeObjectType communicationUsageTypeObjectType = new CommunicationUsageTypeObjectType()
        communicationUsageTypeObjectType.ID.add(wrapCommunicationUsageTypeId())
        commMethod.typeData.add(new CommunicationUsageTypeDataType(primary: this.isPrimary, typeReference: communicationUsageTypeObjectType))
        return emailAddressInfo
    }

    private WorkerObjectType wrapWorkerObjectType(WorkdayClientService workdayClientService) {
        WorkerObjectType objectType = new WorkerObjectType()
        objectType.ID.add(workdayClientService.wrapWidWithWorkerObjectIdType(parentWid))
        return objectType
    }

    private CommunicationUsageTypeObjectIDType wrapCommunicationUsageTypeId() {
        CommunicationUsageTypeObjectIDType communicationUsageTypeObjectIDType = new CommunicationUsageTypeObjectIDType()
        communicationUsageTypeObjectIDType.type = this.usageNamedId
        communicationUsageTypeObjectIDType.value = this.usageType
        return communicationUsageTypeObjectIDType
    }

    void setAddress(String address) {
        if(this.address != address) {
            dirty = true
        }
        this.address = address
    }

    void setComment(String comment) {
        if(this.comment != comment) {
            dirty = true
        }
        this.comment = comment
    }

    void setUsageType(String usageType) {
        if(this.usageType != usageType) {
            dirty = true
        }
        this.usageType = usageType
    }

    void setUsageNamedId(String usageNamedId) {
        if(this.usageNamedId != usageNamedId) {
            dirty = true
        }
        this.usageNamedId = usageNamedId
    }

    void setComments(String comments) {
        if(this.comments != comments) {
            dirty = true
        }
        this.comments = comments
    }

    void setIsPublic(Boolean isPublic) {
        if(this.isPublic != isPublic) {
            dirty = true
        }
        this.isPublic = isPublic
    }

    void setIsPrimary(Boolean isPrimary) {
        if(this.isPrimary != isPrimary) {
            dirty = true
        }
        this.isPrimary = isPrimary
    }

    void resetDirty() {
        dirty = false
    }

    boolean isDirty() {
        return dirty
    }


    @Override
    public String toString() {
        return "Email{" +
                "address='" + address + '\'' +
                ", comment='" + comment + '\'' +
                ", usageType='" + usageType + '\'' +
                ", usageNamedId='" + usageNamedId + '\'' +
                ", usageWid='" + usageWid + '\'' +
                ", comments='" + comments + '\'' +
                ", isPublic=" + isPublic +
                ", isPrimary=" + isPrimary +
                ", dirty=" + dirty +
                '}';
    }
}
