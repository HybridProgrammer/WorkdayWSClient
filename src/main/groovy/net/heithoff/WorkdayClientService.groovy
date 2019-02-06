package net.heithoff

class WorkdayClientService {
    static WorkdayClientService workdayClientService
    // Enter user/password and endpoint information for Proof of Concept
    String wdUser
    String wdPassword

    WorkdayClientService() {
        createWorkdayClientService()
    }

    private WorkdayClientService(String configFilePath) {
        App app = new App(configFilePath)
    }

    def createWorkdayClientService() {
        if(!workdayClientService) {
            workdayClientService = new WorkdayClientService("/Users/jheithof/workday_ws_client.test.properties")
        }
        wdUser = App.config().getProperty("wdUser")
        wdPassword = App.config().getProperty("password")
    }
}
