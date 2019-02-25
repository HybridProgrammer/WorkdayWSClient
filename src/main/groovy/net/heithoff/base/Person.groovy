package net.heithoff.base

import workday.com.bsvc.AcademicAppointeeEnabledObjectIDType
import workday.com.bsvc.WorkerObjectIDType

trait Person {
    String wid
    Map<String, String> referenceIds = [:]

    void setIds(List<?> ids) {
        ids.each {Object id ->
            if(id instanceof WorkerObjectIDType) {
                if (id.type.equalsIgnoreCase("wid")) {
                    wid = id.value
                } else {
                    referenceIds.put(id.type, id.value)
                }
            }
            else if(id instanceof AcademicAppointeeEnabledObjectIDType) {
                if(id.type.equalsIgnoreCase("wid")) {
                    wid = id.value
                }
                else {
                    referenceIds.put(id.type, id.value)
                }
            }
        }
    }
}
