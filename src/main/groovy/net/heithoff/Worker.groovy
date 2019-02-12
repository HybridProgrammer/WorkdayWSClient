package net.heithoff

import groovy.util.logging.Slf4j
import net.heithoff.base.LegalName
import net.heithoff.base.PreferredName
import workday.com.bsvc.GetWorkersRequestType
import workday.com.bsvc.GetWorkersResponseType
import workday.com.bsvc.ResponseFilterType
import workday.com.bsvc.WorkerObjectIDType
import workday.com.bsvc.WorkerObjectType
import workday.com.bsvc.WorkerRequestReferencesType
import workday.com.bsvc.WorkerResponseGroupType
import workday.com.bsvc.WorkerType
import workday.com.bsvc.human_resources.HumanResourcesPort
import workday.com.bsvc.human_resources.ProcessingFaultMsg
import workday.com.bsvc.human_resources.ValidationFaultMsg

import javax.xml.datatype.DatatypeConfigurationException

@Slf4j
class Worker {
    static final WorkdayClientService workdayClientService = WorkdayClientService.getWorkdayClientService()
    WorkerType worker
    String descriptor
    String wid
    LegalName legalName = new LegalName()
    PreferredName preferredName = new PreferredName()

    Worker() {

    }

    Worker(WorkerType workerType) {
        worker = workerType
        descriptor = worker.getWorkerDescriptor()
        List<WorkerObjectIDType> ids = workerType.workerReference.ID
        wid = ids.find {it.type == "WID"}.value
        if(workerType?.workerData?.personalData?.nameData?.legalNameData?.nameDetailData) {
            legalName = new LegalName(wid, workerType.workerData.personalData.nameData.legalNameData.nameDetailData)
        }
    }

    static def findAll() {
        try {

            def resources = workdayClientService.getResources("Human_Resources")

            // Define the paging defaults
            BigDecimal totalPages = 1
            BigDecimal currentPage = 1

            List<Worker> people = []
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
                    Worker person = new Worker(worker)
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

    static Worker findByWorker(String id) {
        try {
            String type = App.properties().get("Worker.default.id.type") ?: "WID" //"Academic_Affiliate_ID"

            return findByWorker(id, type)
        } catch (Exception e) {
            log.error(e.message)
            throw e
        }
    }

    static Worker findByWorker(String id, String type) {
        try {
            def resources = workdayClientService.getResources("Human_Resources")

            WorkerRequestReferencesType reference = new WorkerRequestReferencesType()
            WorkerObjectType refType = new WorkerObjectType()
            refType.ID.add(new WorkerObjectIDType(type: type, value: id))
            reference.workerReference.add(refType)

            GetWorkersRequestType request = new GetWorkersRequestType()
            request.setVersion(workdayClientService.version)
            request.requestReferences = reference

            GetWorkersResponseType response = ((HumanResourcesPort) resources["port"]).getWorkers(request)

            return new Worker(response.getResponseData().worker.first())
        } catch (Exception e) {
            log.error(e.message)
            throw e
        }
    }


    @Override
    public String toString() {
        return "Worker{" +
                "descriptor='" + descriptor + '\'' +
                ", wid='" + wid + '\'' +
                '}';
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Worker worker = (Worker) o

        if (wid != worker.wid) return false

        return true
    }

    int hashCode() {
        return (wid != null ? wid.hashCode() : 0)
    }
}
