package net.heithoff

import groovy.util.logging.Slf4j
import net.heithoff.base.LegalName
import net.heithoff.base.PreferredName

import workday.com.bsvc.AcademicAppointeeEnabledObjectIDType
import workday.com.bsvc.AcademicAppointeeEnabledObjectType
import workday.com.bsvc.AcademicAppointeeRequestReferencesType
import workday.com.bsvc.AcademicAppointeeType
import workday.com.bsvc.GetAcademicAppointeeRequestType
import workday.com.bsvc.GetAcademicAppointeeResponseType
import workday.com.bsvc.PersonNameDetailDataType
import workday.com.bsvc.human_resources.HumanResourcesPort

@Slf4j
class AcademicAppointee {
    static final WorkdayClientService workdayClientService = WorkdayClientService.getWorkdayClientService()
    AcademicAppointeeType academicAppointeeType
    String wid
    String descriptor
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

        resetDirty()
    }

    static AcademicAppointee findByAcadmeicAppointee(String id) {
        try {
            String type = App.properties().get("HRPerson.default.id.type") ?: "WID" //"Academic_Affiliate_ID"

            return findByAcadmeicAppointee(id, type)
        } catch (Exception e) {
            log.error(e.message)
            throw e
        }
    }

    static AcademicAppointee findByAcadmeicAppointee(String id, String type) {
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
        legalName.resetDirty()
    }

    boolean save() {
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
