package net.heithoff

import groovy.util.logging.Slf4j
import net.heithoff.base.ContactInformation
import net.heithoff.base.EmailAddresses
import net.heithoff.base.LegalName
import net.heithoff.base.Person
import net.heithoff.base.PreferredName
import workday.com.bsvc.*
import workday.com.bsvc.human_resources.HumanResourcesPort

import javax.xml.datatype.DatatypeFactory

@Slf4j
class AcademicAppointee implements Person, ContactInformation {
    static final WorkdayClientService workdayClientService = WorkdayClientService.getWorkdayClientService()
    AcademicAppointeeType academicAppointeeType
    Boolean dirty
    String descriptor
    String gender
    GregorianCalendar dateOfBirthCache
    GregorianCalendar dateOfBirth
    GregorianCalendar dateOfDeath
    GregorianCalendar maritalStatusDate
    GregorianCalendar lastMedialExamDate
    GregorianCalendar lastMedicalExamValidTo
    Boolean hispanicOrLatino
    String hukouLocality
    String hukouPostalCode
    String personnelFileAgency
    String medicalExamNotes
    Boolean usesTobacco

    LegalName legalName = new LegalName()
    PreferredName preferredName = new PreferredName()

    AcademicAppointee() {

    }

    AcademicAppointee(AcademicAppointeeType academicAppointeeType) {
        this.academicAppointeeType = academicAppointeeType
        PersonNameDetailDataType name = academicAppointeeType.academicAppointeeData.personData.legalNameData.nameDetailData
        descriptor = name.formattedName
        List<AcademicAppointeeEnabledObjectIDType> ids = academicAppointeeType.academicAppointeeReference.id.first()
        wid = ids.find {it.type == "WID"}.value
        legalName = new LegalName(wid, name)
        name = academicAppointeeType.academicAppointeeData.personData.preferredNameData.nameDetailData
        preferredName = new PreferredName(wid, name)

        this.dateOfBirth = academicAppointeeType.academicAppointeeData.personalInformationData.dateOfBirth?.toGregorianCalendar()
        this.dateOfBirthCache = academicAppointeeType.academicAppointeeData.personalInformationData.dateOfBirth?.toGregorianCalendar()
        this.dateOfDeath = academicAppointeeType.academicAppointeeData.personalInformationData.dateOfDeath?.toGregorianCalendar()
        this.maritalStatusDate = academicAppointeeType.academicAppointeeData.personalInformationData.maritalStatusDate?.toGregorianCalendar()
        this.lastMedialExamDate = academicAppointeeType.academicAppointeeData.personalInformationData.lastMedicalExamDate?.toGregorianCalendar()
        this.lastMedicalExamValidTo = academicAppointeeType.academicAppointeeData.personalInformationData.lastMedicalExamValidTo?.toGregorianCalendar()

        this.hispanicOrLatino = academicAppointeeType.academicAppointeeData.personalInformationData.hispanicOrLatino
        this.hukouLocality = academicAppointeeType.academicAppointeeData.personalInformationData.hukouLocality
        this.hukouPostalCode = academicAppointeeType.academicAppointeeData.personalInformationData.hukouPostalCode
        this.personnelFileAgency = academicAppointeeType.academicAppointeeData.personalInformationData.personnelFileAgency
        this.medicalExamNotes = academicAppointeeType.academicAppointeeData.personalInformationData.medicalExamNotes

        this.usesTobacco = academicAppointeeType.academicAppointeeData.personalInformationData.usesTobacco

        hydrateHomeEmail(this)
        resetDirty()
    }

    static AcademicAppointee findById(String id) {
        try {
            String type = App.properties().get("AcademicAppointee.default.id.type") ?: "WID" //"Academic_Affiliate_ID"

            return findById(id, type)
        } catch (Exception e) {
            log.error(e.message)
            throw e
        }
    }

    static AcademicAppointee findById(String id, String type) {
        try {
            def resources = workdayClientService.getResources("Human_Resources")

            AcademicAppointeeRequestReferencesType reference = new AcademicAppointeeRequestReferencesType()
            AcademicAppointeeEnabledObjectType refType = new AcademicAppointeeEnabledObjectType()
            refType.ID.add(new AcademicAppointeeEnabledObjectIDType(type: type, value: id))
            reference.academicAppointeeReference.add(refType)

            GetAcademicAppointeeRequestType request = new GetAcademicAppointeeRequestType()
            request.setVersion(workdayClientService.version)
            request.requestReferences = reference

            GetAcademicAppointeeResponseType response = ((HumanResourcesPort) resources["port"]).getAcademicAppointee(request)

            return new AcademicAppointee(response.getResponseData().academicAppointee.first())
        } catch (Exception e) {
            log.error(e.message)
            throw e
        }
    }

    void resetDirty() {
        dirty = false
        legalName.resetDirty()
    }

    Boolean getDirty() {
        return isDirty()
    }

    Boolean isDirty() {
        if(dateOfBirth != dateOfBirthCache) {
            return true
        }
        return dirty
    }

    void setDateOfBirth(GregorianCalendar dateOfBirth) {
        if(this.dateOfBirth != dateOfBirth) {
            dirty = true
        }
        this.dateOfBirth = dateOfBirth
    }

    boolean save() {
        if(isDirty()) {
            if(!updateWorkday()) {
                throw new Exception("Failed to update workday Academic Appointee data.")
            }
        }
        if(this.legalName.dirty) {
            if(!legalName.save()) {
                throw new Exception("Failed to update Legal Name")
            }
        }
        if(this.preferredName.dirty) {
            if(!preferredName.save()) {
                throw new Exception("Failed to update preferred Name")
            }
        }

        return true
    }

    boolean updateWorkday() {
        WorkdayClientService workdayClientService = WorkdayClientService.workdayClientService
        ChangePersonalInformationRequestType request = new ChangePersonalInformationRequestType()
        request.version = workdayClientService.version
        request.businessProcessParameters = new BusinessProcessParametersType()
        request.businessProcessParameters.autoComplete = true
        request.businessProcessParameters.runNow = true
        request.changePersonalInformationData = new ChangePersonalInformationBusinessProcessDataType()
        request.changePersonalInformationData.workerReference = new WorkerObjectType()
        request.changePersonalInformationData.workerReference.ID.add(wrapWid())
        request.changePersonalInformationData.personalInformationData = new ChangePersonalInformationDataType()
        request.changePersonalInformationData.personalInformationData.dateOfBirth = DatatypeFactory.newInstance().newXMLGregorianCalendar(this.dateOfBirth)

        def resources = workdayClientService.getResources("Human_Resources")
        try {
            ChangePersonalInformationResponseType response = ((HumanResourcesPort) resources["port"]).changePersonalInformation(request)
        } catch(Exception e) {
            log.error(e.message)
            throw e
        }

        return true
    }

    WorkerObjectIDType wrapWid() {
        WorkerObjectIDType objectIDType = new WorkerObjectIDType()
        objectIDType.type = "WID"
        objectIDType.value = wid

        return objectIDType
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        AcademicAppointee that = (AcademicAppointee) o

        if (wid != that.wid) return false

        return true
    }

    int hashCode() {
        return (wid != null ? wid.hashCode() : 0)
    }
}
