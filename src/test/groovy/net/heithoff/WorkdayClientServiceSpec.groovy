package net.heithoff

import net.heithoff.services.WorkdayClientService
import org.junit.experimental.categories.Category
import spock.lang.Shared
import spock.lang.Specification

@Category(UnitTest)
class WorkdayClientServiceSpec extends Specification {

    @Shared
    WorkdayClientService workdayClientService

    def setupSpec() {
        WorkdayClientService.configPath = "/Users/jheithof/Projects/WorkdayWSClient/workday_ws_client.unittest.properties"
        workdayClientService = WorkdayClientService.getWorkdayClientService()
    }

    def "test spock"() {
        expect:
        true == true
    }

    def "test initialization"() {
        expect:
        workdayClientService.username == "wd_username"
        workdayClientService.wdUser == "wd_username@your_tenant"
        workdayClientService.wdPassword == "your_password"
        workdayClientService.tenant == "your_tenant"
        workdayClientService.host == "your-impl-services1.workday.com"
        workdayClientService.version == "v30.2"
    }

    def "test getServiceUrl"() {
        when:
        String url = workdayClientService.getServiceUrl("Human_Resources")

        then:
        url == "https://your-impl-services1.workday.com/ccx/service/your_tenant/Human_Resources/v30.2"
    }
}
