package net.heithoff.base

class CachedString {
    Boolean dirty
    String value
    Date lastUpdated
    Date lastAccessed

    CachedString() {
        Date now = new Date()
        dirty = false
        lastAccessed = now
        lastAccessed = now
    }

    String getValue() {
        lastAccessed = new Date()
        return value
    }

    void setValue(String value) {
        if(this.value != value) {
            lastUpdated = new Date()
            dirty = true
        }
        this.value = value
    }

    void setValueAndResetDirtyFlag(String value) {
        lastUpdated = new Date()
        dirty = false
        this.value = value
    }
}
