package net.heithoff


import net.heithoff.services.WorkdayClientService
import org.junit.experimental.categories.Category
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

    def "test findById"() {
        when: "we want to search by WID"
        String wid = App.properties().get("test.worker.wid.id").toString()
        Worker worker1 = Worker.findById(wid)
        println worker1

        then:
        worker1
        worker1.wid == wid
        worker1.legalName.firstName == App.properties().get("test.worker.legal.firstName")
        worker1.legalName.middleName == App.properties().get("test.worker.legal.middleName")
        worker1.legalName.lastName == App.properties().get("test.worker.legal.lastName")

        when: "we want to search by custom_ref"
        String id = App.properties().get("test.worker.custom_ref.id").toString()
        String type = App.properties().get("test.worker.custom_ref.type").toString()
        Worker worker2 = Worker.findById(id, type)
        println worker2

        then:
        worker2.wid == wid
        worker2.legalName.firstName == App.properties().get("test.worker.legal.firstName")
        worker2.legalName.middleName == App.properties().get("test.worker.legal.middleName")
        worker2.legalName.lastName == App.properties().get("test.worker.legal.lastName")

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

    def "test update refenceIds"() {
        when: "we want to search by WID"
        String wid = App.properties().get("test2.worker.wid.id").toString()
        Worker worker1 = Worker.findById(wid)
        println worker1

        then:
        worker1

        when: "add a new reference id"
        String existingEmployeeId = worker1.referenceIds.get("Employee_ID")
        worker1.updateRefenceId("Employee_ID", wid)
        boolean value = worker1.save()
        Worker worker2 = Worker.findById(wid)

        then:
        value
        worker2.referenceIds.get("Employee_ID") == wid

        when: "roll back changes"
        worker1.updateRefenceId("Employee_ID", existingEmployeeId)

        then:
        worker1.save()
    }

    def "test update legal names"() {
        given:
        String wid = App.properties().get("test2.worker.wid.id").toString()
        Worker worker1 = Worker.findById(wid)
        println worker1

        when: "update legal first name"
        String expectedValue = worker1.legalName.firstName + "a"
        worker1.legalName.firstName += "a"
        worker1.save()

        then:
        worker1.legalName.firstName == expectedValue

        when: "fetch value from server"
        Worker worker2 = Worker.findById(wid)

        then:
        worker2.legalName.firstName == expectedValue

        when: "update legal middle name"
        expectedValue = worker1.legalName.middleName + "b"
        worker1.legalName.middleName += "b"
        worker1.save()

        then:
        worker1.legalName.middleName == expectedValue

        when: "fetch value from server"
        worker2 = Worker.findById(wid)

        then:
        worker2.legalName.middleName == expectedValue

        when: "update legal last name"
        expectedValue = worker1.legalName.lastName + "c"
        worker1.legalName.lastName += "c"
        worker1.save()

        then:
        worker1.legalName.lastName == expectedValue

        when: "fetch value from server"
        worker2 = Worker.findById(wid)

        then:
        worker2.legalName.lastName == expectedValue
    }

    def "test update preferred names"() {
        given:
        String wid = App.properties().get("test2.worker.wid.id").toString()
        Worker worker1 = Worker.findById(wid)
        println worker1

        when: "update preferred first name"
        String expectedValue = worker1.preferredName.firstName + "a"
        worker1.preferredName.firstName += "a"
        worker1.save()

        then:
        worker1.preferredName.firstName == expectedValue

        when: "fetch value from server"
        Worker worker2 = Worker.findById(wid)

        then:
        worker2.preferredName.firstName == expectedValue

        when: "update preferred middle name"
        expectedValue = worker1.preferredName.middleName + "b"
        worker1.preferredName.middleName += "b"
        worker1.save()

        then:
        worker1.preferredName.middleName == expectedValue

        when: "fetch value from server"
        worker2 = Worker.findById(wid)

        then:
        worker2.preferredName.middleName == expectedValue

        when: "update preferred last name"
        expectedValue = worker1.preferredName.lastName + "c"
        worker1.preferredName.lastName += "c"
        worker1.save()

        then:
        worker1.preferredName.lastName == expectedValue

        when: "fetch value from server"
        worker2 = Worker.findById(wid)

        then:
        worker2.preferredName.lastName == expectedValue
    }

    // Validation error occurred. Invalid instance
    def "test update dob"() {
        given:
        String wid = App.properties().get("test2.worker.wid.id").toString()
        Worker worker1 = Worker.findById(wid)
        println worker1

        when: "update preferred first name"
        toggleDobDayOfYeay(worker1)
        GregorianCalendar expectedValue = worker1.dateOfBirth
        boolean value = worker1.save()

        then:
        value

        when:
        Worker worker2 = Worker.findById(wid)

        then:
        value
        worker2.dateOfBirth.get(Calendar.DAY_OF_YEAR) == expectedValue.get(Calendar.DAY_OF_YEAR)
    }

    private void toggleDobDayOfYeay(Worker worker1) {
        if (worker1.dateOfBirth.get(Calendar.DAY_OF_YEAR) % 2 == 0) {
            worker1.dateOfBirth.add(Calendar.DAY_OF_YEAR, 1)
        } else {
            worker1.dateOfBirth.add(Calendar.DAY_OF_YEAR, 1)
        }
    }


}