package net.heithoff

import groovy.util.logging.Slf4j
import workday.com.bsvc.*
import workday.com.bsvc.human_resources.HumanResourcesPort
import workday.com.bsvc.human_resources.ProcessingFaultMsg
import workday.com.bsvc.human_resources.ValidationFaultMsg

import javax.xml.datatype.DatatypeConfigurationException

@Slf4j
class HRPerson {
    static WorkdayClientService workdayClientService = new WorkdayClientService()
    WorkerType person
    AcademicAppointeeType academicAppointeeType
    String descriptor
    String wid

    HRPerson(WorkerType workerType) {
        person = workerType
        descriptor = person.getWorkerReference().getDescriptor()
        wid = workerType.workerReference.ID.properties.get("WID")
    }

    HRPerson(AcademicAppointeeType academicAppointeeType) {
        descriptor = academicAppointeeType.academicAppointeeData.personData.legalNameData.nameDetailData.formattedName
        wid = academicAppointeeType.academicAppointeeReference.id.find { it.type == "WID"}
    }

    public static void main(String[] args) {
        HRPerson.findAll()
        log.info("works")
    }

    static def findByAcadmeicAppointee(String id) {
        try {
            String type = App.properties().get("HRPerson.default.id.type") ?: "WID" //"Academic_Affiliate_ID"

            return findByAcadmeicAppointee(id, type)
        } catch (Exception e) {
            log.error(e.message)
            throw e
        }
    }

    static def findByAcadmeicAppointee(String id, String type) {
        try {
            def resources = workdayClientService.getResources("Human_Resources")

            AcademicAppointeeRequestReferencesType reference = new AcademicAppointeeRequestReferencesType()
            AcademicAppointeeEnabledObjectType refType = new AcademicAppointeeEnabledObjectType()
            refType.ID.add(new AcademicAppointeeEnabledObjectIDType(type: type, value: id))
            reference.academicAppointeeReference.add(refType)

            GetAcademicAppointeeRequestType request = new GetAcademicAppointeeRequestType()
            request.setVersion(workdayClientService.version)
            request.requestReferences = reference

            GetAcademicAppointeeResponseType response = ((HumanResourcesPort) resources["port"]).getAcademicAppointee(request)

            return new HRPerson(response.getResponseData().academicAppointee.first())
        } catch (Exception e) {
            log.error(e.message)
            throw e
        }
    }

    static def findAll() {
        try {

            def resources = workdayClientService.getResources("Human_Resources")

            // Define the paging defaults
            BigDecimal totalPages = 1
            BigDecimal currentPage = 1

            List<HRPerson> people = []
            // Loop over all of the pages in the web service response
            while (totalPages >= currentPage) {
                // Create a "request" object
                GetWorkersRequestType request = new GetWorkersRequestType()

                // Set the WWS version desired
                request.setVersion("v10")

                // Set the date/time & page parameters in the request
                ResponseFilterType responseFilter = workdayClientService.getDefaultResponseFilterType(currentPage)
                request.setResponseFilter(responseFilter)

                // Set the desired response group(s) to return
                WorkerResponseGroupType responseGroup = new WorkerResponseGroupType()
                responseGroup.setIncludeReference(true)
                request.setResponseGroup(responseGroup)

                // Submit the request creating the "response" object
                GetWorkersResponseType response = ((HumanResourcesPort) resources["port"]).getWorkers(request)

                // Display all Workers
                Iterator<WorkerType> i = response.getResponseData().getWorker()
                        .iterator()
                while (i.hasNext()) {
                    WorkerType worker = i.next()

                    System.out.println(worker.getWorkerReference()
                            .getDescriptor())
                    HRPerson person = new HRPerson(worker)
                    people.add(person)

                }

                // Update page number
                if (totalPages == 1) {
                    totalPages = response.getResponseResults().getTotalPages()
                            .intValue()
                    break
                }
                currentPage++
            }

            return people

        } catch (Exception e) {
            e.printStackTrace()
        } catch (ProcessingFaultMsg e) {
            e.printStackTrace()
        } catch (ValidationFaultMsg e) {
            e.printStackTrace()
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace()
        }
    }


    @Override
    public String toString() {
        return "HRPerson{" +
                "descriptor='" + descriptor + '\'' +
                ", wid='" + wid + '\'' +
                '}';
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        HRPerson hrPerson = (HRPerson) o

        if (wid != hrPerson.wid) return false

        return true
    }

    int hashCode() {
        return (wid != null ? wid.hashCode() : 0)
    }
}
