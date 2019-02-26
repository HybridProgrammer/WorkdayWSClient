package net.heithoff

import org.junit.experimental.categories.Category
import spock.lang.Ignore
import spock.lang.Specification

@Category(IntegrationTest)
class AcademicAppointeeITSpec extends Specification {

    def setupSpec() {
        WorkdayClientService workdayClientService = WorkdayClientService.getWorkdayClientService() // dirty way of loading App config
    }

    def "test Spock"() {
        expect:
        println 'Integration test: should return the correct message'
        true == true
    }

    def "test findById"() {
        when: "we want to search by WID"
        String wid = App.properties().get("test.acadmeicAppointee.wid.id").toString()
        AcademicAppointee affiliate1 = AcademicAppointee.findById(wid)
        println affiliate1

        then:
        affiliate1
        affiliate1.wid == wid
        affiliate1.legalName.firstName == App.properties().get("test.acadmeicAppointee.legal.firstName")
        affiliate1.legalName.middleName == App.properties().get("test.acadmeicAppointee.legal.middleName")
        affiliate1.legalName.lastName == App.properties().get("test.acadmeicAppointee.legal.lastName")

        when: "we want to search by custom_ref"
        String id = App.properties().get("test.acadmeicAppointee.custom_ref.id").toString()
        String type = App.properties().get("test.acadmeicAppointee.custom_ref.type").toString()
        AcademicAppointee affiliate2 = AcademicAppointee.findById(id, type)
        println affiliate2

        then:
        affiliate2.wid == wid
        affiliate2.legalName.firstName == App.properties().get("test.acadmeicAppointee.legal.firstName")
        affiliate2.legalName.middleName == App.properties().get("test.acadmeicAppointee.legal.middleName")
        affiliate2.legalName.lastName == App.properties().get("test.acadmeicAppointee.legal.lastName")

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

    def "test update refenceIds"() {
        when: "we want to search by WID"
        String wid = App.properties().get("test2.acadmeicAppointee.wid.id").toString()
        AcademicAppointee affiliate1 = AcademicAppointee.findById(wid)
        println affiliate1

        then:
        affiliate1

        when: "add a new reference id"
        String existingEmployeeId = affiliate1.referenceIds.get("Academic_Affiliate_ID")
        affiliate1.updateRefenceId("Academic_Affiliate_ID", wid)
        boolean value = affiliate1.save()
        AcademicAppointee affiliate2 = AcademicAppointee.findById(wid)

        then:
        value
        affiliate2.referenceIds.get("Academic_Affiliate_ID") == wid

        when: "roll back changes"
        affiliate1.updateRefenceId("Academic_Affiliate_ID", existingEmployeeId)

        then:
        affiliate1.save()
    }

    def "test update legal names"() {
        given:
        String wid = App.properties().get("test2.acadmeicAppointee.wid.id").toString()
        AcademicAppointee affiliate1 = AcademicAppointee.findById(wid)
        println affiliate1

        when: "update legal first name"
        String expectedValue = affiliate1.legalName.firstName + "a"
        affiliate1.legalName.firstName += "a"
        affiliate1.save()

        then:
        affiliate1.legalName.firstName == expectedValue

        when: "fetch value from server"
        AcademicAppointee affiliate2 = AcademicAppointee.findById(wid)

        then:
        affiliate2.legalName.firstName == expectedValue

        when: "update legal middle name"
        expectedValue = affiliate1.legalName.middleName + "b"
        affiliate1.legalName.middleName += "b"
        affiliate1.save()

        then:
        affiliate1.legalName.middleName == expectedValue

        when: "fetch value from server"
        affiliate2 = AcademicAppointee.findById(wid)

        then:
        affiliate2.legalName.middleName == expectedValue

        when: "update legal last name"
        expectedValue = affiliate1.legalName.lastName + "c"
        affiliate1.legalName.lastName += "c"
        affiliate1.save()

        then:
        affiliate1.legalName.lastName == expectedValue

        when: "fetch value from server"
        affiliate2 = AcademicAppointee.findById(wid)

        then:
        affiliate2.legalName.lastName == expectedValue
    }

//    @Ignore
    def "test update preferred names"() {
        given:
        String wid = App.properties().get("test2.acadmeicAppointee.wid.id").toString()
        AcademicAppointee affiliate1 = AcademicAppointee.findById(wid)
        println affiliate1

        when: "update preferred first name"
        String expectedValue = affiliate1.preferredName.firstName + "a"
        affiliate1.preferredName.firstName += "a"
        affiliate1.save()

        then:
        affiliate1.preferredName.firstName == expectedValue

        when: "fetch value from server"
        AcademicAppointee affiliate2 = AcademicAppointee.findById(wid)

        then:
        affiliate2.preferredName.firstName == expectedValue

        when: "update preferred middle name"
        expectedValue = affiliate1.preferredName.middleName + "b"
        affiliate1.preferredName.middleName += "b"
        affiliate1.save()

        then:
        affiliate1.preferredName.middleName == expectedValue

        when: "fetch value from server"
        affiliate2 = AcademicAppointee.findById(wid)

        then:
        affiliate2.preferredName.middleName == expectedValue

        when: "update preferred last name"
        expectedValue = affiliate1.preferredName.lastName + "c"
        affiliate1.preferredName.lastName += "c"
        affiliate1.save()

        then:
        affiliate1.preferredName.lastName == expectedValue

        when: "fetch value from server"
        affiliate2 = AcademicAppointee.findById(wid)

        then:
        affiliate2.preferredName.lastName == expectedValue
    }

    // Validation error occurred. Invalid instance
    @Ignore
    def "test update dob"() {
        given:
        String wid = App.properties().get("test2.acadmeicAppointee.wid.id").toString()
        AcademicAppointee affiliate1 = AcademicAppointee.findById(wid)
        println affiliate1

        when: "update preferred first name"
        affiliate1.dateOfBirth.add(Calendar.DAY_OF_YEAR, 1)
        GregorianCalendar expectedValue = affiliate1.dateOfBirth
        boolean value = affiliate1.save()

        then:
        value

        when:
        AcademicAppointee affiliate2 = AcademicAppointee.findById(wid)

        then:
        value
        affiliate2.dateOfBirth.get(Calendar.DAY_OF_YEAR) == expectedValue.get(Calendar.DAY_OF_YEAR)
    }
}