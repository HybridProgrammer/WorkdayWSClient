package net.heithoff

import org.junit.experimental.categories.Category
import spock.lang.Specification

@Category(IntegrationTest)
class HRPersonITSpec extends Specification {

    def setupSpec() {
        WorkdayClientService workdayClientService = new WorkdayClientService() // dirty way of loading App config
    }

    def "test Spock"() {
        expect:
        println 'Integration test: should return the correct message'
        true == true
    }

    def "test findAll"() {
        when:
        List<HRPerson> people = HRPerson.findAll()

        then:
        people.size() > 0
    }

    def "test findByAcadmeicAppointee"() {
        when:
        HRPerson person = HRPerson.findByAcadmeicAppointee(App.properties().get("test.findByAcadmeicAppointee.wid.id").toString())
        println person

        then:
        person
    }
}