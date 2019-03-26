package net.heithoff.services

import groovy.util.logging.Slf4j
import net.heithoff.App
import net.heithoff.WorkdayCredentials
import workday.com.bsvc.ResponseFilterType
import workday.com.bsvc.RoleObjectIDType
import workday.com.bsvc.UniversalIdentifierObjectIDType
import workday.com.bsvc.WorkerObjectIDType
import workday.com.bsvc.human_resources.HumanResourcesPort
import workday.com.bsvc.human_resources.HumanResourcesService
import workday.com.bsvc.integrations.IntegrationsPort
import workday.com.bsvc.integrations.IntegrationsService

import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar
import javax.xml.ws.BindingProvider

@Slf4j
class WorkdayClientService {
    static WorkdayClientService workdayClientService
    static String configPath
    final int defaultPageCountSize = 200

    String username
    String wdUser
    String wdPassword
    String tenant
    String host
    String version

    private WorkdayClientService() {
    }

    private WorkdayClientService(String configFilePath) {
        App app = new App(configFilePath)
    }

    static def getWorkdayClientService() {
        if(!configPath) {
            configPath = "/Users/jheithof/workday_ws_client.test.properties"
        }

        if(!workdayClientService) {
            workdayClientService = new WorkdayClientService(configPath)
            workdayClientService.username = App.properties().get("username")
            workdayClientService.wdPassword = App.properties().get("password")
            workdayClientService.tenant = App.properties().get("tenant")
            workdayClientService.wdUser = "${workdayClientService.username}@${workdayClientService.tenant}"
            workdayClientService.host = App.properties().get("host")
            workdayClientService.version = "v${App.properties().get("version")}"
        }

        return workdayClientService
    }

    def getServiceUrl(String service) {
        assert host
        assert tenant
        assert version
        return "https://${host}/ccx/service/${tenant}/${service}/${version}"
    }

    def getResources(String service) {
        def resources
        switch (service) {
            case "Human_Resources":
                resources = prepareHR(service)
                break
            case "Integrations":
                resources = prepareIntegrations(service)
        }

        return resources
    }

    private def prepareIntegrations(String serviceName) {
        // Create the Web Service client stub
        IntegrationsService service = new IntegrationsService()
        IntegrationsPort port = service.getIntegrations()

        LinkedHashMap resources = configureWdConnection(serviceName, port, service)

        return resources
    }

    private def prepareHR(String serviceName) {
        // Create the Web Service client stub
        HumanResourcesService service = new HumanResourcesService()
        HumanResourcesPort port = service.getHumanResources()

        LinkedHashMap resources = configureWdConnection(serviceName, port, service)

        return resources
    }

    private LinkedHashMap configureWdConnection(String serviceName, def port, def service) {
        def resources = [:]
        String wdEndpoint = getServiceUrl(serviceName)

        log.debug("Starting...")
        // Add the WorkdayCredentials handler to the client stub
        WorkdayCredentials.addWorkdayCredentials((BindingProvider) port, wdUser, wdPassword)

        // Assign the Endpoint URL
        Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext()
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, wdEndpoint)

        resources.put("service", service)
        resources.put("port", port)
        resources.put("requestContext", requestContext)
        resources
    }

    ResponseFilterType getDefaultResponseFilterType(BigDecimal page) {
        // Set the current date/time
        GregorianCalendar cal = new GregorianCalendar()
        XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal)

        ResponseFilterType responseFilter = new ResponseFilterType()
        responseFilter.setAsOfEntryDateTime(xmlCal)
        responseFilter.setAsOfEffectiveDate(xmlCal)
        responseFilter.setPage(BigDecimal.valueOf(page))
        responseFilter.setCount(BigDecimal.valueOf(defaultPageCountSize))
    }

    XMLGregorianCalendar generateEffectiveDate() {
        Date now = new Date()
        GregorianCalendar c = new GregorianCalendar()
        c.setTime(now)
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(c)
    }

    /**
     * Workday Docs:
     * A reference to the ID of the person through one of its active person types, such as worker,
     * student, affiliate, external committee member, and so on. The ID consists of a type attribute,
     * which should be set to one of either "Employee_ID", "Contingent_Worker_ID", "Student_ID",
     * etc, and a value attribute, such as "04345".
     *
     * I don't think the docs are correct, wid also works
     * @param wid
     * @return
     */
    RoleObjectIDType wrapWid(String wid) {
        RoleObjectIDType pk = new RoleObjectIDType()
        pk.value = wid
        pk.type = "WID"
        return pk
    }

    WorkerObjectIDType wrapWidWithWorkerObjectIdType(String wid) {
        WorkerObjectIDType pk = new WorkerObjectIDType()
        pk.value = wid
        pk.type = "WID"
        return pk
    }

    UniversalIdentifierObjectIDType wrapWidWithUID(String wid) {
        UniversalIdentifierObjectIDType pk = new UniversalIdentifierObjectIDType()
        pk.value = wid
        pk.type = "WID"
        return pk
    }
}
