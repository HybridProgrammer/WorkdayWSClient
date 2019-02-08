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
        AcademicAppointee affiliate1 = AcademicAppointee.findByAcadmeicAppointee(wid)
        println affiliate1

        then:
        affiliate1
        affiliate1.wid == wid
        affiliate1.legalName.firstName == App.properties().get("test.findByAcadmeicAppointee.legal.firstName")
        affiliate1.legalName.middleName == App.properties().get("test.findByAcadmeicAppointee.legal.middleName")
        affiliate1.legalName.lastName == App.properties().get("test.findByAcadmeicAppointee.legal.lastName")

        when: "we want to search by custom_ref"
        String id = App.properties().get("test.findByAcadmeicAppointee.custom_ref.id").toString()
        String type = App.properties().get("test.findByAcadmeicAppointee.custom_ref.type").toString()
        AcademicAppointee affiliate2 = AcademicAppointee.findByAcadmeicAppointee(id, type)
        println affiliate2

        then:
        affiliate2.wid == wid
        affiliate2.legalName.firstName == App.properties().get("test.findByAcadmeicAppointee.legal.firstName")
        affiliate2.legalName.middleName == App.properties().get("test.findByAcadmeicAppointee.legal.middleName")
        affiliate2.legalName.lastName == App.properties().get("test.findByAcadmeicAppointee.legal.lastName")

        when: "we modify any attribute other than wid equality is always true when wids match"
        affiliate1.legalName.firstName = "something else"
        affiliate1.legalName.middleName = "something else"
        affiliate1.legalName.lastName = "something else"

        then:
        affiliate1.legalName.firstName != affiliate2.legalName.firstName
        affiliate1.legalName.middleName != affiliate2.legalName.middleName
        affiliate1.legalName.lastName != affiliate2.legalName.lastName
        affiliate1 == affiliate2 // only checks wid, this may change in the future
    }

    def "test update legal names"() {
        given:
        String wid = App.properties().get("test2.findByAcadmeicAppointee.wid.id").toString()
        AcademicAppointee affiliate1 = AcademicAppointee.findByAcadmeicAppointee(wid)
        println affiliate1

        when: "update legal first name"
        String expectedValue = affiliate1.legalName.firstName + "a"
        affiliate1.legalName.firstName += "a"
        affiliate1.save()

        then:
        affiliate1.legalName.firstName == expectedValue

        when: "fetch value from server"
        AcademicAppointee affiliate2 = AcademicAppointee.findByAcadmeicAppointee(wid)

        then:
        affiliate2.legalName.firstName == expectedValue

        when: "update legal middle name"
        expectedValue = affiliate1.legalName.middleName + "b"
        affiliate1.legalName.middleName += "b"
        affiliate1.save()

        then:
        affiliate1.legalName.middleName == expectedValue

        when: "fetch value from server"
        affiliate2 = AcademicAppointee.findByAcadmeicAppointee(wid)

        then:
        affiliate2.legalName.middleName == expectedValue

        when: "update legal last name"
        expectedValue = affiliate1.legalName.lastName + "c"
        affiliate1.legalName.lastName += "c"
        affiliate1.save()

        then:
        affiliate1.legalName.lastName == expectedValue

        when: "fetch value from server"
        affiliate2 = AcademicAppointee.findByAcadmeicAppointee(wid)

        then:
        affiliate2.legalName.lastName == expectedValue


    }
}