package net.heithoff

import workday.com.bsvc.GetWorkersRequestType
import workday.com.bsvc.GetWorkersResponseType
import workday.com.bsvc.ResponseFilterType
import workday.com.bsvc.WorkerResponseGroupType
import workday.com.bsvc.WorkerType
import workday.com.bsvc.human_resources.HumanResourcesPort
import workday.com.bsvc.human_resources.HumanResourcesService
import workday.com.bsvc.human_resources.ProcessingFaultMsg
import workday.com.bsvc.human_resources.ValidationFaultMsg

import javax.xml.datatype.DatatypeConfigurationException
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar
import javax.xml.ws.BindingProvider

class HRPerson {
    public static void main(String[] args) {
        App app = new App("/Users/jheithof/workday_ws_client.test.properties")
        HRPerson.findAll()
        println "works"
    }

    static def findAll() {
        try {

            // Enter user/password and endpoint information for Proof of Concept
            final String wdUser = App.config().getProperty("wdUser")
            final String wdPassword = App.config().getProperty("password")

            // final String wdEndpoint =
            // "https://e2-impl-cci.workday.com/ccx/service/exampleTenant/Human_Resources/v16";
            final String wdEndpoint = App.config().getProperty("wdEndpoint");

            System.out.println("Starting...");

            // Create the Web Service client stub
            HumanResourcesService service = new HumanResourcesService();
            HumanResourcesPort port = service.getHumanResources();

            // Add the WorkdayCredentials handler to the client stub
            WorkdayCredentials.addWorkdayCredentials((BindingProvider) port, wdUser, wdPassword);

            // Assign the Endpoint URL
            Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
            requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, wdEndpoint);

            // Define the paging defaults
            final int countSize = 200;
            int totalPages = 1;
            int currentPage = 1;

            // Set the current date/time
            GregorianCalendar cal = new GregorianCalendar();
            XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);

            // Loop over all of the pages in the web service response
            while (totalPages >= currentPage) {
                // Create a "request" object
                GetWorkersRequestType request = new GetWorkersRequestType();

                // Set the WWS version desired
                request.setVersion("v10");

                // Set the date/time & page parameters in the request
                ResponseFilterType responseFilter = new ResponseFilterType();
                responseFilter.setAsOfEntryDateTime(xmlCal);
                responseFilter.setAsOfEffectiveDate(xmlCal);
                responseFilter.setPage(BigDecimal.valueOf(currentPage));
                responseFilter.setCount(BigDecimal.valueOf(countSize));
                request.setResponseFilter(responseFilter);

                // Set the desired response group(s) to return
                WorkerResponseGroupType responseGroup = new WorkerResponseGroupType();
                responseGroup.setIncludeReference(true);
                request.setResponseGroup(responseGroup);

                // Submit the request creating the "response" object
                GetWorkersResponseType response = port.getWorkers(request);

                // Display all Workers
                Iterator<WorkerType> i = response.getResponseData().getWorker()
                        .iterator();
                while (i.hasNext()) {
                    WorkerType worker = i.next();

                    System.out.println(worker.getWorkerReference()
                            .getDescriptor());

                }

                // Update page number
                if (totalPages == 1) {
                    totalPages = response.getResponseResults().getTotalPages()
                            .intValue();
                    break;
                }
                currentPage++;
            }

        } catch (Exception e) {
            e.printStackTrace()
        } catch (ProcessingFaultMsg e) {
            e.printStackTrace();
        } catch (ValidationFaultMsg e) {
            e.printStackTrace();
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
    }
}
