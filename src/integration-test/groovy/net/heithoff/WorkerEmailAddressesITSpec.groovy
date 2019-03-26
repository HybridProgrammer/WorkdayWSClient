package net.heithoff

import net.heithoff.base.Email
import net.heithoff.services.WorkdayClientService
import org.junit.Ignore
import org.junit.experimental.categories.Category
import spock.lang.Specification

@Category(IntegrationTest)
class WorkerEmailAddressesITSpec extends Specification {

    def setupSpec() {
        WorkdayClientService workdayClientService = WorkdayClientService.getWorkdayClientService() // dirty way of loading App config
    }

    def "test Spock"() {
        expect:
        true == true
    }

    def "test update email address"() {
        given:
        String wid = App.properties().get("test2.worker.wid.id").toString()
        Worker worker1 = Worker.findById(wid)
        println worker1

        when: "update work email address"
        worker1.workEmail.address = "a" + worker1.workEmail.address
        boolean value = worker1.save()

        then:
        value

        when:
        Worker worker2 = Worker.findById(wid)

        then:
        value
        worker2.workEmail.address == worker1.workEmail.address
    }

    def "test add / remove email address"() {
        given:
        String wid = App.properties().get("test2.worker.wid.id").toString()
        Worker worker1 = Worker.findById(wid)
        println worker1

        when: "update work email address"
        Email expectedFinalEmail = worker1.workEmail
        Email newEmail = TestUtil.quickNewEmail(worker1)
        newEmail.isPrimary = true
        worker1.addEmail(newEmail)

        then:
        worker1.workEmail == newEmail
        worker1.workEmail != expectedFinalEmail

        when:
        boolean value = worker1.save()

        then:
        value

        when: "try to add the same email address a second time"
        Integer nEmails = worker1.emailAddresses.size()
        worker1.addEmail(newEmail)

        then: "end up with the same number of email addresses"
        Exception ex = thrown()
        ex.message.contains("already exists")
        worker1.emailAddresses.size() == nEmails

        when:
        Worker worker2 = Worker.findById(wid)

        then:
        worker2.workEmail == newEmail
        worker2.emailAddresses.contains(expectedFinalEmail)

        when: "now let's clean up and remove the newEmail"
        worker1.removeEmail(newEmail)
        worker1.save()
        worker2 = Worker.findById(wid)

        then:
        worker1.workEmail.address.equalsIgnoreCase(expectedFinalEmail.address)
        worker2.workEmail.address.equalsIgnoreCase(expectedFinalEmail.address)
        !worker2.emailAddresses.contains(newEmail)
    }

    def "test create new email address"() {
        given:
        String wid = App.properties().get("test2.worker.wid.id").toString()
        Worker worker1 = Worker.findById(wid)
        println worker1

        when: "update work email address"
        Email newEmail = TestUtil.quickNewEmail(worker1)
        worker1.addEmail(newEmail)

        then:
        worker1.save()
        worker1.emailAddresses.size() >= 2
    }

    def "test swap primary email address"() {
        given:
        String wid = App.properties().get("test2.worker.wid.id").toString()
        Worker worker1 = Worker.findById(wid)
        Integer nEmails = worker1.emailAddresses.size()
        assert worker1.emailAddresses.findAll {it.usageType.equalsIgnoreCase("work")}?.size() >= 2
        Email expectedPrimaryWorkEmail = worker1.emailAddresses.find { !it.isPrimary }
        assert expectedPrimaryWorkEmail != worker1.workEmail

        when: "update work email address"
        worker1.workEmail = expectedPrimaryWorkEmail
        worker1.save()
        worker1 = Worker.findById(wid)

        then:
        expectedPrimaryWorkEmail == worker1.workEmail
        nEmails == worker1.emailAddresses.size()
    }

    def "test delete primary email address"() {
        given:
        String wid = App.properties().get("test2.worker.wid.id").toString()
        Worker worker1 = Worker.findById(wid)
        assert worker1.emailAddresses.findAll {it.usageType.equalsIgnoreCase("work")}?.size() >= 2
        Email deletedPrimaryWorkEmail = worker1.workEmail

        when: "update work email address"
        Integer nEmails = worker1.emailAddresses.size()
        worker1.removeEmail(deletedPrimaryWorkEmail)
        worker1.save()
        worker1 = Worker.findById(wid)

        then:
        worker1.workEmail != deletedPrimaryWorkEmail
        worker1.emailAddresses.size() == nEmails - 1
    }

    def "test delete secondary email address"() {
        given:
        String wid = App.properties().get("test2.worker.wid.id").toString()
        Worker worker1 = Worker.findById(wid)
        println worker1

        when: "update work email address"
        Email newEmail = TestUtil.quickNewEmail(worker1)
        worker1.addEmail(newEmail)

        then:
        worker1.save()
        newEmail.isPrimary == false

        when:
        Integer nEmails = worker1.emailAddresses.size()
        worker1.removeEmail(newEmail)

        then:
        worker1.save()
        worker1.emailAddresses.size() == nEmails - 1
    }

    @Ignore
    def "test clean emails"() {
        given:
        String wid = App.properties().get("test2.worker.wid.id").toString()
        Worker worker1 = Worker.findById(wid)
        println worker1

        when: "update work email address"
        worker1.workEmail.address = Math.random() + "username@example.com"
        worker1.emailAddresses.each {
            if(!it.address.equalsIgnoreCase(worker1.workEmail.address)) {
                it.delete = true
            }
        }

        then:
        worker1.save()
        worker1.emailAddresses.size() == 1
    }
}
