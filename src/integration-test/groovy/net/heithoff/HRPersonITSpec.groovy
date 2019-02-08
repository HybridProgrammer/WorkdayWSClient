package net.heithoff

import org.junit.experimental.categories.Category
import spock.lang.Specification

@Category(IntegrationTest)
class HRPersonITSpec extends Specification {

    def setupSpec() {
        WorkdayClientService workdayClientService = WorkdayClientService.getWorkdayClientService() // dirty way of loading App config
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
        person1.wid == wid
        person1.academicAppointee.legalName.firstName == App.properties().get("test.findByAcadmeicAppointee.legal.firstName")
        person1.academicAppointee.legalName.middleName == App.properties().get("test.findByAcadmeicAppointee.legal.middleName")
        person1.academicAppointee.legalName.lastName == App.properties().get("test.findByAcadmeicAppointee.legal.lastName")

        when: "we want to search by custom_ref"
        String id = App.properties().get("test.findByAcadmeicAppointee.custom_ref.id").toString()
        String type = App.properties().get("test.findByAcadmeicAppointee.custom_ref.type").toString()
        HRPerson person2 = HRPerson.findByAcadmeicAppointee(id, type)
        println person2

        then:
        person2.wid == wid
        person2.academicAppointee.legalName.firstName == App.properties().get("test.findByAcadmeicAppointee.legal.firstName")
        person2.academicAppointee.legalName.middleName == App.properties().get("test.findByAcadmeicAppointee.legal.middleName")
        person2.academicAppointee.legalName.lastName == App.properties().get("test.findByAcadmeicAppointee.legal.lastName")

        when: "we modify any attribute other than wid equality is always true when wids match"
        person1.academicAppointee.legalName.firstName = "something else"
        person1.academicAppointee.legalName.middleName = "something else"
        person1.academicAppointee.legalName.lastName = "something else"

        then:
        person1.academicAppointee.legalName.firstName != person2.academicAppointee.legalName.firstName
        person1.academicAppointee.legalName.middleName != person2.academicAppointee.legalName.middleName
        person1.academicAppointee.legalName.lastName != person2.academicAppointee.legalName.lastName
        person1 == person2 // only checks wid, this may change in the future
    }

    def "test update legal names"() {
        given:
        String wid = App.properties().get("test2.findByAcadmeicAppointee.wid.id").toString()
        HRPerson person1 = HRPerson.findByAcadmeicAppointee(wid)
        println person1

        when: "update legal first name"
        String expectedValue = person1.academicAppointee.legalName.firstName + "a"
        person1.academicAppointee.legalName.firstName += "a"
        person1.save()

        then:
        person1.academicAppointee.legalName.firstName == expectedValue

        when: "fetch value from server"
        HRPerson person2 = HRPerson.findByAcadmeicAppointee(wid)

        then:
        person2.academicAppointee.legalName.firstName == expectedValue

        when: "update legal middle name"
        expectedValue = person1.academicAppointee.legalName.middleName + "b"
        person1.academicAppointee.legalName.middleName += "b"
        person1.save()

        then:
        person1.academicAppointee.legalName.middleName == expectedValue

        when: "fetch value from server"
        person2 = HRPerson.findByAcadmeicAppointee(wid)

        then:
        person2.academicAppointee.legalName.middleName == expectedValue

        when: "update legal last name"
        expectedValue = person1.academicAppointee.legalName.lastName + "c"
        person1.academicAppointee.legalName.lastName += "c"
        person1.save()

        then:
        person1.academicAppointee.legalName.lastName == expectedValue

        when: "fetch value from server"
        person2 = HRPerson.findByAcadmeicAppointee(wid)

        then:
        person2.academicAppointee.legalName.lastName == expectedValue


    }
}