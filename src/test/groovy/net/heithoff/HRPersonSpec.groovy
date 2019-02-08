package net.heithoff

import net.heithoff.base.LegalName
import org.junit.experimental.categories.Category
import spock.lang.Specification

@Category(UnitTest)
class HRPersonSpec extends Specification{

    def "test spock"() {
        expect:
        println 'Unit test: should return the correct message'
        true == true
    }

    def "test isDirty Legal Name"() {
        given:
        LegalName name = new LegalName()

        when:
        boolean value = name.isDirty()

        then:
        value == false

        when:
        name.firstName += "a"

        then:
        name.isDirty()

        when:
        name.resetDirty()
        assert name.isDirty() == false
        name.middleName += "a"

        then:
        name.isDirty()

        when:
        name.resetDirty()
        assert name.isDirty() == false
        name.lastName += "a"

        then:
        name.isDirty()

    }
}
