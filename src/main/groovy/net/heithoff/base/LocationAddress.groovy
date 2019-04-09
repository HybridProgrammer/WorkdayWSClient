package net.heithoff.base

/**
 * Created by jason on 2/4/16.
 */
class LocationAddress {
    String addressType
    String addressText

    public Map asMap() {
        this.class.declaredFields.findAll { !it.synthetic }.collectEntries {
            [ (it.name):this."$it.name" ]
        }
    }
}
