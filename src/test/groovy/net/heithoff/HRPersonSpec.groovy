package net.heithoff

import org.junit.experimental.categories.Category
import spock.lang.Specification

@Category(UnitTest)
class HRPersonSpec extends Specification{

    def "test spock"() {
        expect:
        println 'Unit test: should return the correct message'
        true == true
    }
}
