package net.heithoff

class WorkdayClientService {
    static WorkdayClientService workdayClientService
    static String configPath
    // Enter user/password and endpoint information for Proof of Concept
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
        version = App.config().getProperty("version")
    }

    def getServiceUrl(String service) {
        return "https://${host}/ccx/service/${tenant}/${service}/v${version}"
    }
}
