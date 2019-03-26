package net.heithoff.base

import groovy.util.logging.Slf4j
import net.heithoff.Exceptions.NotImplementedException
import net.heithoff.services.WorkdayClientService
import workday.com.bsvc.AcademicAppointeeEnabledObjectIDType
import workday.com.bsvc.PutReferenceRequestType
import workday.com.bsvc.PutReferenceResponseType
import workday.com.bsvc.ReferenceIDDataType
import workday.com.bsvc.ReferenceIndexObjectIDType
import workday.com.bsvc.WorkerObjectIDType
import workday.com.bsvc.integrations.IntegrationsPort

@Slf4j
trait Person {
    String wid
    Map<String, String> referenceIds = [:]
    Map<String, String> dirtyReferenceIds = [:]

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

    void updateRefenceId(String type, String value) {
        if(type.equalsIgnoreCase("wid")) {
            throw new Exception("Cannot update wid")
        }

        if(referenceIds.containsKey(type)) {
            String val = referenceIds.get(type)
            if(!val.equalsIgnoreCase(value) && !dirtyReferenceIds.containsKey(type)) {
                dirtyReferenceIds.put(type, val)
            }
        }
        else {
            throw new NotImplementedException("Unable to add new reference Ids. Can only support updates to existing ids")
        }

        referenceIds.put(type, value)
    }

    boolean saveReferenceIds() {
        WorkdayClientService workdayClientService = WorkdayClientService.workdayClientService
        dirtyReferenceIds.each { String type, String oldId ->
            String newId = referenceIds.get(type)
            if(newId != null) {
                PutReferenceRequestType request = new PutReferenceRequestType()
                request.version = workdayClientService.version
//                request.referenceIDReference = new ReferenceIndexObjectType()
//                request.referenceIDReference.ID.add(wrapIndexedObjectidType())
                request.referenceIDData = new ReferenceIDDataType()
                request.referenceIDData.setReferenceIDType(type)
                request.referenceIDData.setID(oldId)
                request.referenceIDData.setNewID(newId)

                def resources = workdayClientService.getResources("Integrations")
                try {
                    PutReferenceResponseType response = ((IntegrationsPort) resources["port"]).putReference(request)
                    log.debug("${type} updated to ${newId}")
                } catch(Exception e) {
                    log.error(e.message)
                    throw e
                }
            }
        }

        dirtyReferenceIds.clear()

        return true
    }

    private ReferenceIndexObjectIDType wrapIndexedObjectidType() {
        ReferenceIndexObjectIDType idType = new ReferenceIndexObjectIDType()
        idType.type = "WID"
        idType.value = wid
        idType
    }
}
