package net.heithoff

import groovy.util.logging.Slf4j
import net.heithoff.base.Email
import net.heithoff.base.LegalName
import net.heithoff.base.PreferredName
import workday.com.bsvc.BusinessProcessParametersType
import workday.com.bsvc.ChangePersonalInformationBusinessProcessDataType
import workday.com.bsvc.ChangePersonalInformationDataType
import workday.com.bsvc.ChangePersonalInformationRequestType
import workday.com.bsvc.ChangePersonalInformationResponseType
import workday.com.bsvc.EmailAddressInformationDataType
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
import javax.xml.datatype.DatatypeFactory

@Slf4j
class Worker {
    static final WorkdayClientService workdayClientService = WorkdayClientService.getWorkdayClientService()
    WorkerType worker
    Boolean dirty
    String descriptor
    String wid
    GregorianCalendar dateOfBirthCache
    GregorianCalendar dateOfBirth
    LegalName legalName = new LegalName()
    PreferredName preferredName = new PreferredName()
    Email workEmail
    Email personalEmail
    List<Email> emailAddresses

    Worker() {

    }

    Worker(WorkerType workerType) {
        worker = workerType
        descriptor = worker.getWorkerDescriptor()
        List<WorkerObjectIDType> ids = workerType.workerReference.ID
        wid = ids.find { it.type == "WID" }.value
        if (workerType?.workerData?.personalData?.nameData?.legalNameData?.nameDetailData) {
            legalName = new LegalName(wid, workerType.workerData.personalData.nameData.legalNameData.nameDetailData)
        }
        if (workerType?.workerData?.personalData?.nameData?.preferredNameData?.nameDetailData) {
            preferredName = new PreferredName(wid, workerType.workerData.personalData.nameData.preferredNameData.nameDetailData)
        }

        this.dateOfBirth = workerType.workerData.personalData.birthDate?.toGregorianCalendar()
        this.dateOfBirthCache = workerType.workerData.personalData.birthDate?.toGregorianCalendar()
        loadEmailData(workerType)

        resetDirty()
    }

    private void loadEmailData(WorkerType workerType) {
        emailAddresses = []
        workerType.workerData.personalData.contactData.emailAddressData.each { EmailAddressInformationDataType emailAddressData ->
            Email email = new Email(wid, emailAddressData)
            this.emailAddresses.add(email)
        }

        resetPrimaryEmailAddresses()
    }

    private void resetPrimaryEmailAddresses() {
        emailAddresses.each { Email email ->
            if (!email.delete && email.isPrimary && "WORK".equalsIgnoreCase(email.usageType)) {
                workEmail = email
            }

            if (!email.delete && email.isPrimary && "HOME".equalsIgnoreCase(email.usageType)) {
                personalEmail = email
            }
        }
        // if there is no primary work email select the first work email available
        if (!workEmail) {
            workEmail = emailAddresses.find { !it.delete && "WORK".equalsIgnoreCase(it.usageType) }
            if(workEmail) workEmail.isPrimary = true
        }

        if (!personalEmail) {
            personalEmail = emailAddresses.find { !it.delete && "HOME".equalsIgnoreCase(it.usageType) }
            if(personalEmail) personalEmail.isPrimary = true
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
                responseGroup.setIncludePersonalInformation(true)
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

    static Worker findById(String id) {
        try {
            String type = App.properties().get("Worker.default.id.type") ?: "WID" //"Academic_Affiliate_ID"

            return findById(id, type)
        } catch (Exception e) {
            log.error(e.message)
            throw e
        }
    }

    static Worker findById(String id, String type) {
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

    void resetDirty() {
        dirty = false
        legalName.resetDirty()
    }

    Boolean getDirty() {
        return isDirty()
    }

    Boolean isDirty() {
        if (dateOfBirth != dateOfBirthCache) {
            return true
        }
        return dirty
    }

    void setDateOfBirth(GregorianCalendar dateOfBirth) {
        if (this.dateOfBirth != dateOfBirth) {
            dirty = true
        }
        this.dateOfBirth = dateOfBirth
    }

    Email getEmailByAddress(String address) {
        Email email = emailAddresses.find {it.address.equalsIgnoreCase(address) && !it.delete}
        return email
    }

    void addEmail(Email email) {
        if (!email.parentWid) {
            email.parentWid = wid
        }

        if (!email.isValid()) {
            throw new Exception("Email is not valid, errors: " + email.errors)
        }

        if(getEmailByAddress(email.address)) {
            throw new Exception("Email address already exists")
        }

        if (email.isPrimary) {
            switch (email.usageType.toLowerCase()) {
                case "work":
                    if (workEmail) {
                        workEmail.isPrimary = false
                        workEmail = email
                    }
                    break
                case "home":
                    if (personalEmail) {
                        personalEmail.isPrimary = false
                        personalEmail = email
                    }
                    break
            }
        }

        emailAddresses.add(email)
    }

    void removeEmail(Email email) {
        if (workEmail.equals(email)) {
            workEmail = null
        }
        if (personalEmail.equals(email)) {
            personalEmail = null
        }

        email.isPrimary = false
        email.delete = true
        resetPrimaryEmailAddresses()
    }

    boolean save() {
        if (isDirty()) {
            if (!updateWorkday()) {
                throw new Exception("Failed to update workday Academic Appointee data.")
            }
        }
        if (this.legalName.dirty) {
            if (!legalName.save()) {
                throw new Exception("Failed to update Legal Name")
            }
        }
        if (this.preferredName.dirty) {
            if (!preferredName.save()) {
                throw new Exception("Failed to update preferred Name")
            }
        }

        workEmail.save()
        workEmail.resetDirty()

        def dirtyEmails = emailAddresses.findAll { it.isDirty() }
        List<EmailAddressInformationDataType> emailInfo = []
        dirtyEmails.each {
            emailInfo.add(Email.wrapEmailInformation(it))
        }
        Email.save(emailInfo, wid)
        dirtyEmails.each {
            it.resetDirty()
        }

        purgeDeletedEmails()

//        // must processes updates before deletes
//        def dirtyEmailsUpdates = emailAddresses.findAll { it.isDirty() && !it.delete }
//        dirtyEmailsUpdates.each {
//            it.save()
//        }
//        def dirtyEmailsDeletes = emailAddresses.findAll { it.isDirty() && it.delete }
//        dirtyEmailsDeletes.each {
//            it.save()
//        }



        return true
    }

    private void purgeDeletedEmails() {
        emailAddresses = emailAddresses.findAll { !it.delete }
    }

    boolean updateWorkday() {
        WorkdayClientService workdayClientService = WorkdayClientService.workdayClientService
        ChangePersonalInformationRequestType request = new ChangePersonalInformationRequestType()
        request.version = workdayClientService.version
        request.businessProcessParameters = new BusinessProcessParametersType()
        request.businessProcessParameters.autoComplete = true
        request.businessProcessParameters.runNow = true
        request.changePersonalInformationData = new ChangePersonalInformationBusinessProcessDataType()
        request.changePersonalInformationData.workerReference = new WorkerObjectType()
        request.changePersonalInformationData.workerReference.ID.add(wrapWid())
        request.changePersonalInformationData.personalInformationData = new ChangePersonalInformationDataType()
        request.changePersonalInformationData.personalInformationData.dateOfBirth = DatatypeFactory.newInstance().newXMLGregorianCalendar(this.dateOfBirth)

        def resources = workdayClientService.getResources("Human_Resources")
        try {
            ChangePersonalInformationResponseType response = ((HumanResourcesPort) resources["port"]).changePersonalInformation(request)
        } catch (Exception e) {
            log.error(e.message)
            throw e
        }

        return true
    }

    WorkerObjectIDType wrapWid() {
        WorkerObjectIDType objectIDType = new WorkerObjectIDType()
        objectIDType.type = "WID"
        objectIDType.value = wid

        return objectIDType
    }

    void setWorkEmail(Email workEmail) {
        if(this.workEmail && this.workEmail != workEmail) {
            this.workEmail.isPrimary = false
        }
        this.workEmail = workEmail
    }

    void setPersonalEmail(Email personalEmail) {
        if(this.personalEmail && this.personalEmail != personalEmail) {
            this.personalEmail.isPrimary = false
        }
        this.personalEmail = personalEmail
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
