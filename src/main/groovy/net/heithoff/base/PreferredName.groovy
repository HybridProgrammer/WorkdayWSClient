package net.heithoff.base

import groovy.util.logging.Slf4j
import net.heithoff.services.WorkdayClientService
import net.heithoff.traits.Name
import workday.com.bsvc.*
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

        resetDirty()
        return true
    }
}
