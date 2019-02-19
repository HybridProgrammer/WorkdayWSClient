package net.heithoff.base

import workday.com.bsvc.EmailAddressInformationDataType
import workday.com.bsvc.WorkerType

trait EmailAddresses {
    Email workEmail
    Email personalEmail
    List<Email> emailAddresses

    void loadEmailData(Person parent, WorkerType workerType) {
        emailAddresses = []
        workerType.workerData.personalData.contactData.emailAddressData.each { EmailAddressInformationDataType emailAddressData ->
            Email email = new Email(parent, emailAddressData)
            this.emailAddresses.add(email)
        }

        resetPrimaryEmailAddresses()
    }

    void resetPrimaryEmailAddresses() {
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

    void setWorkEmail(Email workEmail) {
        if(this.workEmail && this.workEmail != workEmail) {
            this.workEmail.isPrimary = false
        }
        this.@workEmail = workEmail
        if(workEmail) {
            this.workEmail.isPrimary = true
            this.workEmail.primaryChanged = true
        }
    }

    void setPersonalEmail(Email personalEmail) {
        if(this.personalEmail && this.personalEmail != personalEmail) {
            this.personalEmail.isPrimary = false
        }
        this.@personalEmail = personalEmail
        if(personalEmail) {
            this.personalEmail.isPrimary = true
            this.personalEmail.primaryChanged = true
        }
    }

    void saveEmails(String parentWid) {
        List<Email> dirtyEmails = []
        Boolean replaceAll = hasPrimaryEmailChange()
        if (replaceAll) {
            dirtyEmails = emailAddresses.findAll { !it.delete }
        } else {
            dirtyEmails = emailAddresses.findAll { it.isDirty() }
        }
        List<EmailAddressInformationDataType> emailInfo = []
        dirtyEmails.each {
            EmailAddressInformationDataType emailInformation = Email.wrapEmailInformation(it, replaceAll)
            if (replaceAll) {
                emailInformation.emailReference = null // must remove reference when replace all is true
            }
            emailInfo.add(emailInformation)
        }
        Email.save(emailInfo, parentWid)
        dirtyEmails.each {
            it.resetDirty()
        }

        purgeDeletedEmails()
    }

    boolean hasPrimaryEmailChange() {
        return emailAddresses.find { it.primaryChanged } != null
    }

    private void purgeDeletedEmails() {
        emailAddresses = emailAddresses.findAll { !it.delete }
    }

    Email getEmailByAddress(String address) {
        Email email = emailAddresses.find {it.address.equalsIgnoreCase(address) && !it.delete}
        return email
    }

    void addEmail(Email email) {
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

}