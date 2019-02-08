package net.heithoff.traits

import net.heithoff.AcademicAppointee

trait SearchAcademicAppointee {
    static abstract AcademicAppointee findByAcadmeicAppointee(String id)
    static abstract AcademicAppointee findByAcadmeicAppointee(String id, String type)
}
