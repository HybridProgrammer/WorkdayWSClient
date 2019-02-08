package net.heithoff.traits

trait Name {

    String firstName
    String middleName
    String lastName

    Boolean dirty = false

    void setFirstName(String firstName) {
        if(this.firstName != firstName) {
            dirty = true
        }
        this.firstName = firstName
    }

    void setMiddleName(String middleName) {
        if(this.middleName != middleName) {
            dirty = true
        }
        this.middleName = middleName
    }

    void setLastName(String lastName) {
        if(this.lastName != lastName) {
            dirty = true
        }
        this.lastName = lastName
    }

    void resetDirty() {
        dirty = false
    }

    boolean isDirty() {
        return dirty
    }

}