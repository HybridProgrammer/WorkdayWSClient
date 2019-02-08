package net.heithoff.base

import groovy.util.logging.Slf4j
import net.heithoff.WorkdayClientService
import net.heithoff.traits.Name
import workday.com.bsvc.BusinessProcessParametersType
import workday.com.bsvc.ChangePreferredNameBusinessProcessDataType
import workday.com.bsvc.ChangePreferredNameRequestType
import workday.com.bsvc.ChangePreferredNameResponseType
import workday.com.bsvc.PersonNameDetailDataType
import workday.com.bsvc.RoleObjectType
import workday.com.bsvc.UniversalIdentifierObjectIDType
import workday.com.bsvc.UniversalIdentifierObjectType
import workday.com.bsvc.human_resources.HumanResourcesPort

@Slf4j
class PreferredName implements Name {
    String parentWid
    String wid
    CountryReference country

    PreferredName() {
        resetDirty()
    }

    PreferredName(String parentWid, PersonNameDetailDataType name) {
        this.parentWid = parentWid
        this.firstName = name.firstName
        this.middleName = name.middleName
        this.lastName = name.lastName

        country = new CountryReference(name.countryReference)

        resetDirty()
    }

    boolean save() {
        //change_legal_name
        WorkdayClientService workdayClientService = WorkdayClientService.workdayClientService
        ChangePreferredNameRequestType request = new ChangePreferredNameRequestType()
        request.businessProcessParameters = new BusinessProcessParametersType()
        request.businessProcessParameters.autoComplete = true
        request.businessProcessParameters.runNow = true
        request.version = workdayClientService.version
        request.changePreferredNameData = new ChangePreferredNameBusinessProcessDataType()
        request.changePreferredNameData.personReference = new RoleObjectType()
        request.changePreferredNameData.personReference.ID.add(workdayClientService.wrapWid(parentWid))
        request.changePreferredNameData.useLegalNameAsPreferredName = false
        request.changePreferredNameData.nameData = new PersonNameDetailDataType()
        request.changePreferredNameData.nameData.countryReference = country.countryObjectType
        request.changePreferredNameData.nameData.firstName = this.firstName
        request.changePreferredNameData.nameData.middleName = this.middleName
        request.changePreferredNameData.nameData.lastName = this.lastName

        def resources = workdayClientService.getResources("Human_Resources")
        try {
            ChangePreferredNameResponseType response = ((HumanResourcesPort) resources["port"]).changePreferredName(request)
        } catch(Exception e) {
            log.error(e.message)
            throw e
        }

        return true
    }
}
