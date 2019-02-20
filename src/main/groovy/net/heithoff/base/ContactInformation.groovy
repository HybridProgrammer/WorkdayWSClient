package net.heithoff.base

import groovy.util.logging.Slf4j
import workday.com.bsvc.GetChangeHomeContactInformationRequestReferencesType
import workday.com.bsvc.GetChangeHomeContactInformationRequestType
import workday.com.bsvc.GetChangeHomeContactInformationResponseType
import workday.com.bsvc.RoleObjectIDType
import workday.com.bsvc.RoleObjectType
import workday.com.bsvc.human_resources.HumanResourcesPort

@Slf4j
trait ContactInformation implements EmailAddresses {


    def hydrateHomeEmail(Person person) {
        try {
            def resources = workdayClientService.getResources("Human_Resources")

            GetChangeHomeContactInformationRequestType request = new GetChangeHomeContactInformationRequestType()
            request.version = workdayClientService.version
            request.requestReferences = new GetChangeHomeContactInformationRequestReferencesType()
            RoleObjectType refType = new RoleObjectType()
//            RoleObjectIDType pk = new RoleObjectIDType()
//            pk.value = id
//            pk.type = type
            refType.ID.add(workdayClientService.wrapWid(person.wid))
            request.requestReferences.personReference.add(refType)

            GetChangeHomeContactInformationResponseType response = ((HumanResourcesPort) resources["port"]).getChangeHomeContactInformation(request)

            return loadEmailData(person, response.responseData)
        } catch (Exception e) {
            log.error(e.message)
            throw e
        }
    }

}