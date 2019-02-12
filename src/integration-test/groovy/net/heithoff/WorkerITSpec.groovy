package net.heithoff

import org.junit.experimental.categories.Category
import spock.lang.Ignore
import spock.lang.Specification

@Category(IntegrationTest)
class WorkerITSpec extends Specification {

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
        List<Worker> people = Worker.findAll()

        then:
        people.size() > 0
    }

    def "test findByWorker"() {
        when: "we want to search by WID"
        String wid = App.properties().get("test.findByWorker.wid.id").toString()
        Worker worker1 = Worker.findByWorker(wid)
        println worker1

        then:
        worker1
        worker1.wid == wid
        worker1.legalName.firstName == App.properties().get("test.findByWorker.legal.firstName")
        worker1.legalName.middleName == App.properties().get("test.findByWorker.legal.middleName")
        worker1.legalName.lastName == App.properties().get("test.findByWorker.legal.lastName")

        when: "we want to search by custom_ref"
        String id = App.properties().get("test.findByWorker.custom_ref.id").toString()
        String type = App.properties().get("test.findByWorker.custom_ref.type").toString()
        Worker worker2 = Worker.findByWorker(id, type)
        println worker2

        then:
        worker2.wid == wid
        worker2.legalName.firstName == App.properties().get("test.findByWorker.legal.firstName")
        worker2.legalName.middleName == App.properties().get("test.findByWorker.legal.middleName")
        worker2.legalName.lastName == App.properties().get("test.findByWorker.legal.lastName")

        when: "we modify any attribute other than wid equality is always true when wids match"
        worker1.legalName.firstName = "something else"
        worker1.legalName.middleName = "something else"
        worker1.legalName.lastName = "something else"

        then:
        worker1.legalName.firstName != worker2.legalName.firstName
        worker1.legalName.middleName != worker2.legalName.middleName
        worker1.legalName.lastName != worker2.legalName.lastName
        worker1 == worker2 // only checks wid, this may change in the future
    }

    @Ignore
    def "test update legal names"() {
        given:
        String wid = App.properties().get("test2.findByWorker.wid.id").toString()
        Worker affiliate1 = Worker.findByWorker(wid)
        println affiliate1

        when: "update legal first name"
        String expectedValue = affiliate1.legalName.firstName + "a"
        affiliate1.legalName.firstName += "a"
        affiliate1.save()

        then:
        affiliate1.legalName.firstName == expectedValue

        when: "fetch value from server"
        Worker worker2 = Worker.findByWorker(wid)

        then:
        worker2.legalName.firstName == expectedValue

        when: "update legal middle name"
        expectedValue = affiliate1.legalName.middleName + "b"
        affiliate1.legalName.middleName += "b"
        affiliate1.save()

        then:
        affiliate1.legalName.middleName == expectedValue

        when: "fetch value from server"
        worker2 = Worker.findByWorker(wid)

        then:
        worker2.legalName.middleName == expectedValue

        when: "update legal last name"
        expectedValue = affiliate1.legalName.lastName + "c"
        affiliate1.legalName.lastName += "c"
        affiliate1.save()

        then:
        affiliate1.legalName.lastName == expectedValue

        when: "fetch value from server"
        worker2 = Worker.findByWorker(wid)

        then:
        worker2.legalName.lastName == expectedValue
    }

    @Ignore
    def "test update preferred names"() {
        given:
        String wid = App.properties().get("test2.findByWorker.wid.id").toString()
        Worker affiliate1 = Worker.findByWorker(wid)
        println affiliate1

        when: "update preferred first name"
        String expectedValue = affiliate1.preferredName.firstName + "a"
        affiliate1.preferredName.firstName += "a"
        affiliate1.save()

        then:
        affiliate1.preferredName.firstName == expectedValue

        when: "fetch value from server"
        Worker worker2 = Worker.findByWorker(wid)

        then:
        worker2.preferredName.firstName == expectedValue

        when: "update preferred middle name"
        expectedValue = affiliate1.preferredName.middleName + "b"
        affiliate1.preferredName.middleName += "b"
        affiliate1.save()

        then:
        affiliate1.preferredName.middleName == expectedValue

        when: "fetch value from server"
        worker2 = Worker.findByWorker(wid)

        then:
        worker2.preferredName.middleName == expectedValue

        when: "update preferred last name"
        expectedValue = affiliate1.preferredName.lastName + "c"
        affiliate1.preferredName.lastName += "c"
        affiliate1.save()

        then:
        affiliate1.preferredName.lastName == expectedValue

        when: "fetch value from server"
        worker2 = Worker.findByWorker(wid)

        then:
        worker2.preferredName.lastName == expectedValue
    }

    // Validation error occurred. Invalid instance
    @Ignore
    def "test update dob"() {
        given:
        String wid = App.properties().get("test2.findByWorker.wid.id").toString()
        Worker affiliate1 = Worker.findByWorker(wid)
        println affiliate1

        when: "update preferred first name"
        affiliate1.dateOfBirth.add(Calendar.DAY_OF_YEAR, 1)
        GregorianCalendar expectedValue = affiliate1.dateOfBirth
        boolean value = affiliate1.save()

        then:
        value

        when:
        Worker worker2 = Worker.findByWorker(wid)

        then:
        value
        worker2.dateOfBirth.get(Calendar.DAY_OF_YEAR) == expectedValue.get(Calendar.DAY_OF_YEAR)
    }
}