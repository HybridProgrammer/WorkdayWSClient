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
        when: "we want to search by WID"
        String wid = App.properties().get("test.findByAcadmeicAppointee.wid.id").toString()
        HRPerson person1 = HRPerson.findByAcadmeicAppointee(wid)
        println person1

        then:
        person1

        when: "we want to search by custom_ref"
        String id = App.properties().get("test.findByAcadmeicAppointee.custom_ref.id").toString()
        String type = App.properties().get("test.findByAcadmeicAppointee.custom_ref.type").toString()
        HRPerson person2 = HRPerson.findByAcadmeicAppointee(id, type)
        println person2

        then:
        person2
        person1 == person2
    }
}