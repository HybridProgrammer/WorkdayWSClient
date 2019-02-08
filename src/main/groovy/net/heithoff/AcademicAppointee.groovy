package net.heithoff

import net.heithoff.base.LegalName
import net.heithoff.base.PreferredName

class AcademicAppointee {
    String wid
    LegalName legalName = new LegalName()
    PreferredName preferredName = new PreferredName()

    boolean save() {
        if(this.legalName.dirty) {
            return legalName.save()
        }
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        AcademicAppointee that = (AcademicAppointee) o

        if (wid != that.wid) return false

        return true
    }

    int hashCode() {
        return (wid != null ? wid.hashCode() : 0)
    }
}
