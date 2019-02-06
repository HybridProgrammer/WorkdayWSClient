package net.heithoff

import groovy.util.logging.Slf4j
import workday.com.bsvc.ResponseFilterType
import workday.com.bsvc.human_resources.HumanResourcesPort
import workday.com.bsvc.human_resources.HumanResourcesService

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

    WorkdayClientService() {
        createWorkdayClientService()
    }

    private WorkdayClientService(String configFilePath) {
        App app = new App(configFilePath)
    }

    def createWorkdayClientService() {
        if(!configPath) {
            configPath = "/Users/jheithof/workday_ws_client.test.properties"
        }

        if(!workdayClientService) {
            workdayClientService = new WorkdayClientService(configPath)
        }
        username = App.config().getProperty("username")
        wdPassword = App.config().getProperty("password")
        tenant = App.config().getProperty("tenant")
        wdUser = "${username}@${tenant}"
        host = App.config().getProperty("host")
        version = "v${App.config().getProperty("version")}"
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
                resources = prePareHR(service)
                break;
        }

        return resources
    }

    private def prePareHR(String serviceName) {
        def resources = [:]
        String wdEndpoint = getServiceUrl(serviceName)

        log.debug("Starting...")

        // Create the Web Service client stub
        HumanResourcesService service = new HumanResourcesService()
        HumanResourcesPort port = service.getHumanResources()

        // Add the WorkdayCredentials handler to the client stub
        WorkdayCredentials.addWorkdayCredentials((BindingProvider) port, wdUser, wdPassword)

        // Assign the Endpoint URL
        Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext()
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, wdEndpoint)

        resources.put("service", service)
        resources.put("port", port)
        resources.put("requestContext", requestContext)

        return resources
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
}
