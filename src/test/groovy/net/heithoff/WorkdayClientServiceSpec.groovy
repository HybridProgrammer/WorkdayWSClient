package net.heithoff

import org.junit.experimental.categories.Category
import spock.lang.Specification

@Category(UnitTest)
class WorkdayClientServiceSpec extends Specification {

    def setupSpec() {
        WorkdayClientService.configPath = "/Users/jheithof/Projects/WorkdayWSClient/workday_ws_client.unittest.properties"
    }

    def "test spock"() {
        expect:
        true == true
    }

    def "test initialization"() {
        when:
        WorkdayClientService workdayClientService = new WorkdayClientService()

        then:
        workdayClientService.username == "wd_username"
        workdayClientService.wdUser == "wd_username@your_tenant"
        workdayClientService.wdPassword == "your_password"
        workdayClientService.tenant == "your_tenant"
        workdayClientService.host == "your-impl-services1.workday.com"
    }
}
