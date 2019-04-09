package net.heithoff.base

/**
 * Created by jason on 1/15/16.
 */
class LocationUsage {
    String desciptor
    String wid
    String id

    public Map asMap() {
        this.class.declaredFields.findAll { !it.synthetic }.collectEntries {
            [ (it.name):this."$it.name" ]
        }
    }
}
