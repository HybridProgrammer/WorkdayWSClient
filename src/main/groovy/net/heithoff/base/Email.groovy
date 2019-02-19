package net.heithoff.base

import groovy.transform.AutoClone
import groovy.util.logging.Slf4j
import net.heithoff.AcademicAppointee
import net.heithoff.App
import net.heithoff.WorkdayClientService
import net.heithoff.Worker
import workday.com.bsvc.*
import workday.com.bsvc.human_resources.HumanResourcesPort

@Slf4j
@AutoClone
class Email {
    Person person
    String wid
    String address
    String usageType
    String usageNamedId
    String usageWid
    String comments
    Boolean isPublic
    Boolean isPrimary
    Boolean primaryChanged
    Boolean dirty
    Boolean delete
    List<String> errors = []

    Email() {
        isPublic = false
        isPrimary = false
        primaryChanged = false
        dirty = false
        delete = false
    }

    Email(Person parent, EmailAddressInformationDataType emailAddressData) {
        this.person = parent
        if(emailAddressData.emailReference) {
            List<EmailReferenceObjectIDType> ids = emailAddressData.emailReference.ID
            wid = ids.find {it.type == "WID"}.value
            this.wid = wid
        }

        delete = false
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
        this.primaryChanged = false
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

    static boolean save(List<EmailAddressInformationDataType> emailInfo, String parentWid) {
        WorkdayClientService workdayClientService = WorkdayClientService.workdayClientService
        MaintainContactInformationForPersonEventRequestType request = new MaintainContactInformationForPersonEventRequestType()
        request.version = workdayClientService.version
        request.addOnly = true
        request.businessProcessParameters = new BusinessProcessParametersType()
        request.businessProcessParameters.autoComplete = true
        request.businessProcessParameters.runNow = true
        request.maintainContactInformationData = new ContactInformationForPersonEventDataType()
        request.maintainContactInformationData.effectiveDate = workdayClientService.generateEffectiveDate()
        request.maintainContactInformationData.workerReference = wrapWorkerObjectType(workdayClientService, parentWid)
        request.maintainContactInformationData.workerContactInformationData = new ContactInformationDataType()
        emailInfo.each {
            request.maintainContactInformationData.workerContactInformationData.emailAddressData.add(it)
        }

        def resources = workdayClientService.getResources("Human_Resources")
        try {
            MaintainContactInformationForPersonEventResponseType response = ((HumanResourcesPort) resources["port"]).maintainContactInformation(request)
        } catch(Exception e) {
            log.error(e.message)
            throw e
        }

        log.debug("saved all email addresses")
        return true

    }

    boolean save() {
        boolean result = save([wrapEmailInformation()], person.wid)

        if(result) {
            resetDirty()
            log.debug("saved email: " + this.address)
        }
        else {
            log.error("failed to save email: " + this.address)
        }
        return result

    }

    static EmailAddressInformationDataType wrapEmailInformation(Email email, Boolean replaceAll) {
        EmailAddressInformationDataType emailAddressInfo = new EmailAddressInformationDataType(emailAddress: email.address)
        if(email.delete) {
            emailAddressInfo.delete = email.delete
            if(!email.wid) {
                if(email.person instanceof Worker) {
                    def worker = Worker.findById(email.person.wid)
                    email.wid = worker.emailAddresses.find {it.address == email.address}?.wid
                }
                else if(email.person instanceof AcademicAppointee) {
                    def academicAppointee = AcademicAppointee.findById(email.person.wid)
//                    email.wid = AcademicAppointee.emailAddresses.find {it.address == email.address}?.wid
                }
            }
            if(email.wid) {
                emailAddressInfo.emailReference = new EmailReferenceObjectType()
                emailAddressInfo.emailReference.ID.add(new EmailReferenceObjectIDType(type: "WID", value: email.wid))
            }
        }
        emailAddressInfo.doNotReplaceAll = !replaceAll
        CommunicationMethodUsageInformationDataType commMethod = new CommunicationMethodUsageInformationDataType(public: email.isPublic)
        emailAddressInfo.usageData.add(commMethod)
        CommunicationUsageTypeObjectType communicationUsageTypeObjectType = new CommunicationUsageTypeObjectType()
        communicationUsageTypeObjectType.ID.add(wrapCommunicationUsageTypeId(email))
        commMethod.typeData.add(new CommunicationUsageTypeDataType(primary: email.isPrimary, typeReference: communicationUsageTypeObjectType))
        return emailAddressInfo
    }

    EmailAddressInformationDataType wrapEmailInformation() {
        return wrapEmailInformation(this, false)
    }

    static WorkerObjectType wrapWorkerObjectType(WorkdayClientService workdayClientService, String parentWid) {
        WorkerObjectType objectType = new WorkerObjectType()
        objectType.ID.add(workdayClientService.wrapWidWithWorkerObjectIdType(parentWid))
        return objectType
    }

    WorkerObjectType wrapWorkerObjectType(WorkdayClientService workdayClientService) {
        return wrapWorkerObjectType(workdayClientService, person.wid)
    }

    static CommunicationUsageTypeObjectIDType wrapCommunicationUsageTypeId(Email email) {
        CommunicationUsageTypeObjectIDType communicationUsageTypeObjectIDType = new CommunicationUsageTypeObjectIDType()
        communicationUsageTypeObjectIDType.type = email.usageNamedId
        communicationUsageTypeObjectIDType.value = email.usageType
        return communicationUsageTypeObjectIDType
    }

    CommunicationUsageTypeObjectIDType wrapCommunicationUsageTypeId() {
        return wrapCommunicationUsageTypeId(this)
    }

    boolean isValid() {
        errors.clear()
        if(!usageType || usageType.isEmpty()) {
            errors.push("usageType cannot be null.")
        }
        if(!usageNamedId || usageNamedId.isEmpty()) {
            errors.push("usageNamedId cannot be null.")
        }
        if(!address || address.isEmpty()) {
            errors.push("address cannot be null.")
        }
        if(!person.wid || person.wid.isEmpty()) {
            errors.push("parentWid cannot be null.")
        }

        return errors.size() == 0
    }

    void setAddress(String address) {
        if(this.address != address) {
            dirty = true
        }
        this.address = address
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
            if(this.isPrimary) {
                primaryChanged = true
            }
        }
        this.isPrimary = isPrimary
    }

    void setDelete(Boolean delete) {
        if(this.delete != delete) {
            dirty = true
        }
        this.delete = delete
    }

    void resetDirty() {
        dirty = false
        primaryChanged = false
    }

    boolean isDirty() {
        return dirty
    }


    @Override
    public String toString() {
        return "Email{" +
                "address='" + address + '\'' +
                ", comments='" + comments + '\'' +
                ", usageType='" + usageType + '\'' +
                ", wid='" + wid + '\'' +
                ", comments='" + comments + '\'' +
                ", isPublic=" + isPublic +
                ", isPrimary=" + isPrimary +
                ", delete=" + delete +
                ", dirty=" + dirty +
                '}';
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Email email = (Email) o

        if (address != email.address) return false
        if (comments != email.comments) return false
        if (isPrimary != email.isPrimary) return false
        if (isPublic != email.isPublic) return false
        if (person.wid != email.person.wid) return false
        if (usageNamedId != email.usageNamedId) return false
        if (usageType != email.usageType) return false
        if (usageWid != email.usageWid) return false

        return true
    }

    int hashCode() {
        int result
        result = (person.wid != null ? person.wid.hashCode() : 0)
        result = 31 * result + (address != null ? address.hashCode() : 0)
        result = 31 * result + (usageType != null ? usageType.hashCode() : 0)
        result = 31 * result + (usageNamedId != null ? usageNamedId.hashCode() : 0)
        result = 31 * result + (usageWid != null ? usageWid.hashCode() : 0)
        result = 31 * result + (comments != null ? comments.hashCode() : 0)
        result = 31 * result + (isPublic != null ? isPublic.hashCode() : 0)
        result = 31 * result + (isPrimary != null ? isPrimary.hashCode() : 0)
        return result
    }
}
