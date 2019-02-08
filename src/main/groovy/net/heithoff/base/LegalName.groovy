package net.heithoff.base

import groovy.util.logging.Slf4j
import net.heithoff.WorkdayClientService
import net.heithoff.traits.Name
import workday.com.bsvc.BusinessProcessParametersType
import workday.com.bsvc.ChangeLegalNameBusinessProcessDataType
import workday.com.bsvc.ChangeLegalNameRequestType
import workday.com.bsvc.ChangeLegalNameResponseType
import workday.com.bsvc.PersonNameDetailDataType
import workday.com.bsvc.RoleObjectIDType
import workday.com.bsvc.RoleObjectType
import workday.com.bsvc.human_resources.HumanResourcesPort

@Slf4j
class LegalName implements Name {
    String wid
    CountryReference country

    LegalName() {
        resetDirty()
    }

    LegalName(String wid, PersonNameDetailDataType name) {
        this.wid = wid
        this.firstName = name.firstName
        this.middleName = name.middleName
        this.lastName = name.lastName

        country = new CountryReference(name.countryReference)
    }

    boolean save() {
        //change_legal_name
        WorkdayClientService workdayClientService = WorkdayClientService.workdayClientService
        ChangeLegalNameRequestType request = new ChangeLegalNameRequestType()
        request.businessProcessParameters = new BusinessProcessParametersType()
        request.businessProcessParameters.autoComplete = true
        request.businessProcessParameters.runNow = true
        request.version = workdayClientService.version
        request.changeLegalNameData = new ChangeLegalNameBusinessProcessDataType()
        request.changeLegalNameData.personReference = new RoleObjectType()
        request.changeLegalNameData.personReference.ID.add(getPrimaryKey())
        request.changeLegalNameData.effectiveDate = workdayClientService.generateEffectiveDate()
        request.changeLegalNameData.nameData = new PersonNameDetailDataType()
        request.changeLegalNameData.nameData.countryReference = country.countryObjectType
        request.changeLegalNameData.nameData.firstName = this.firstName
        request.changeLegalNameData.nameData.middleName = this.middleName
        request.changeLegalNameData.nameData.lastName = this.lastName

        def resources = workdayClientService.getResources("Human_Resources")
        try {
            ChangeLegalNameResponseType response = ((HumanResourcesPort) resources["port"]).changeLegalName(request)
        } catch(Exception e) {
            log.error(e.message)
            throw e
        }
    }

    RoleObjectIDType getPrimaryKey() {
        RoleObjectIDType pk = new RoleObjectIDType()
        pk.value = wid
        pk.type = "WID"
        return pk
    }

}