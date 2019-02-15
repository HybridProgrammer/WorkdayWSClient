package net.heithoff.base

import groovy.util.logging.Slf4j
import net.heithoff.App
import workday.com.bsvc.CommunicationMethodUsageDataType
import workday.com.bsvc.CommunicationMethodUsageInformationDataType
import workday.com.bsvc.CommunicationUsageTypeDataType
import workday.com.bsvc.CommunicationUsageTypeObjectIDType
import workday.com.bsvc.EmailAddressInformationDataType

@Slf4j
class Email {
    String address
    String comment
    String usageType
    String usageNamedId
    String usageWid
    String comments
    Boolean isPublic
    Boolean isPrimary

    Email(EmailAddressInformationDataType emailAddressData) {
        if(emailAddressData.usageData.size() > 1) {
            log.warn("This library assumes their is only one usageData per email, Open an issue in github if this assumption is wrong, https://github.com/HybridProgrammer/WorkdayWSClient")
        }
        if(emailAddressData.usageData.first().typeData.size() > 1) {
            log.warn("This library assumes their is only one typeData per email, Open an issue in github if this assumption is wrong, https://github.com/HybridProgrammer/WorkdayWSClient")
        }

        CommunicationMethodUsageInformationDataType communicationMethodUsageInformation = emailAddressData.usageData.first()
        CommunicationUsageTypeDataType usageTypeData = communicationMethodUsageInformation.typeData.first()
        this.address = emailAddressData.emailAddress
        this.isPublic = communicationMethodUsageInformation.public
        this.isPrimary = usageTypeData.primary
        this.comments = communicationMethodUsageInformation.comments
        usageTypeData.typeReference.ID.each { CommunicationUsageTypeObjectIDType id ->
            usageNamedId = App.properties().get("email.communication.usage.type.id", "Communication_Usage_Type_ID").toString()
            if(usageNamedId.equalsIgnoreCase(id.type)) {
                usageType = id.value
            }

            if("WID".equalsIgnoreCase(id.type)) {
                usageWid = id.value
            }
        }
    }
}
