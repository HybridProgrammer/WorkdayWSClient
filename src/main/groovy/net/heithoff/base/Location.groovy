package net.heithoff.base

import net.heithoff.services.IsoCodeService

class Location {
    String descriptor           //Location_Reference Descripotor
    String wid                  //Location_Refreence WID
    String id                   //Location_Reference Location_ID
    String name                 //Location_Name
    String previousName
    String typeId               //Location_Type_Reference Location_Type_ID
    String typeWid              //Location_Type_Reference WID
    String typeDescriptor       //Location_Type_Reference Descriptor
    String superiorDescriptor   //Superior_Location_Reference Descriptor
    String superiorWid          //Superior_Location_Reference WID
    String superiorId           //Superior_Location_Reference Location_ID
    String integrationI         //Integration_ID_Data WDI
    String integrationWid       //Integration_ID_Data WID.
    String integrationFAMISId   //FAMIS Reference
    String timeZoneId
    String timeZoneDescriptor
    String timeZoneWid
    String countryISO2 = (Locale.US).getCountry()
    String countryISO3 = (Locale.US).getISO3Country()
    String postalCode
    String city
    String state
    String countryRegionId
    String phoneFormatted
    String phoneInternationalCode
    String phoneAreaCode
    String phoneNumber
    String phoneType

    Boolean inactive            //Null indicates this value has never been set. This is an important distinction in the mapping function
    Float latitude
    Float longitude
    Float altitude

    Location superiorLocation

    List<LocationUsage> locationUsages = new ArrayList<>()
    List<Location> subordinateLocations = new ArrayList<>()
    List<LocationAddress> addresses = new ArrayList<>()

    Boolean isAddressUpdated = false
    Boolean isPhoneUpdated = false
    Boolean isDirty = false
    Boolean isNew = false
    Boolean isUpdated = false
    Boolean hasDuplicate = false

    List<String> auditLog = new ArrayList<>()

    private static IsoCodeService isoCodeService = new IsoCodeService()

//    void addToLocationUsages(WDLocationUsage locationUsage) {
//        isDirty = true
//        updateAuditLog("locationUsages Changed adding " + locationUsages.id)
//        locationUsages.add(locationUsage)
//    }

    void setId(String id) {
        isDirty = true
        updateAuditLog("id Changed from " + this.id + " to " + id)
        this.id = id
    }

    void setInactive(boolean value) {
        isDirty = true
        this.inactive = value
        updateAuditLog("inactive flag Changed")
    }

    void setPhoneFormatted(String phoneFormatted) {
        isDirty = true
        this.phoneFormatted = phoneFormatted
        updateAuditLog("phoneFormatted Changed")
    }

    void setPhoneInternationalCode(String phoneInternationalCode) {
        isDirty = true
        this.phoneInternationalCode = phoneInternationalCode
        updateAuditLog("phoneInternationalCode Changed")
    }

    void setPhoneAreaCode(String phoneAreaCode) {
        if(isAddressUpdated) {
            isDirty = true
        }
        isPhoneUpdated = true
        this.phoneAreaCode = phoneAreaCode
        updateAuditLog("phoneAreaCode Changed")
    }

    void setPhoneNumber(String phoneNumber) {
        if(isAddressUpdated) {
            isDirty = true
        }
        isPhoneUpdated = true
        this.phoneNumber = phoneNumber
        updateAuditLog("phoneNumber Changed")
    }

    void setPhoneType(String phoneType) {
        if(isAddressUpdated) {
            isDirty = true
        }
        isPhoneUpdated = true
        this.phoneType = phoneType
        updateAuditLog("phoneType Changed")
    }

    void setInactive(Boolean inactive) {
        isDirty = true
        this.inactive = inactive
        updateAuditLog("inactive Changed")
    }

    void setIntegrationFAMISId(String integrationFAMISId) {
        isDirty = true
        this.integrationFAMISId = integrationFAMISId
        updateAuditLog("integrationFAMISId Changed")
    }

    void setDescriptor(String descriptor) {
        isDirty = true
        this.descriptor = descriptor
        updateAuditLog("descriptor Changed")
    }

    void setName(String name) {
        isDirty = true
        previousName = this.name ?: ""
        this.name = name

        String audit = "name Changed"
        if(name.endsWith("*")) {
            audit += " - was marked for future removal. From " + previousName + " to " + this.name
        }
        else if(previousName.equalsIgnoreCase(this.name)) {
            audit += " - minor changes from " + previousName + " to " + this.name
        }
        else {
            audit += " - from " + previousName + " to " + this.name
        }
        updateAuditLog(audit)
    }

    void setCountryISO2(String countryISO2) {
        isDirty = true
        this.countryISO2 = countryISO2
//        this.countryISO3 = (new Locale("en", countryISO2)).getISO3Country()
        this.@countryISO3 = isoCodeService.iso2CountryCodeToIso3CountryCode(this.countryISO2)
        updateAuditLog("countryISO2 & countryISO3 Changed")
    }

    void setCountryISO3(String countryISO3) {
        isDirty = true
        this.countryISO3 = countryISO3
        this.@countryISO2 = isoCodeService.iso3CountryCodeToIso2CountryCode(this.countryISO3)
        updateAuditLog("countryISO3 & countryISO2 Changed")
    }

    void setCountryRegionId(String countryRegionId) {
        isDirty = true
        if(countryRegionId.contains("-") && countryRegionId.split('-').size() > 1) {
            String[] values = countryRegionId.split('-')
            countryISO3 = values[0]
            state = values[1]
        }
        else {
            throw new Exception("Country Rewgion Id is in the wrong format")
        }

        this.countryRegionId = countryRegionId
        updateAuditLog("countryISO3 && state && countryRegionId Changed")
    }

//    boolean addAddress(WDLocationAddress address) {
//        boolean result = addresses.add(address)
//        if(result) {
//            if(isPhoneUpdated) {
//                isDirty = true
//            }
//            isAddressUpdated = true
//        }
//
//        updateAuditLog("addresses Added")
//        return result
//    }
//
//    boolean removeAddress(WDLocationAddress address) {
//        boolean result = addresses.remove(address)
//        if(result) {
//            if(isPhoneUpdated) {
//                isDirty = true
//            }
//            isAddressUpdated = true
//        }
//
//        updateAuditLog("addresses Removed")
//        return result
//    }

    void setLatitude(Float latitude) {
        isDirty = true
        this.latitude = latitude
        updateAuditLog("latitude Changed")
    }

    void setLongitude(Float longitude) {
        isDirty = true
        this.longitude = longitude
        updateAuditLog("longitude Changed")
    }

    String getCountryRegionId() {
        if(countryISO3 && state) {
            return countryISO3 + "-" + state
        }

        return null
    }

    public Map asMap() {
        this.class.declaredFields.findAll { !it.synthetic }.collectEntries {
            [ (it.name):this."$it.name" ]
        }
    }

//    boolean equals(FamisProperty famisProperty) {
//        if(famisProperty == null) {
//            log.info "famisPropertyById is null. Not a match"
//            return false
//        }
//        else if(
//        famisProperty.getFormattedBuildingName().equals(this.name)
////                && famisPropertyById.getSupperiorLocation().equals(this.superiorDescriptor)
//        ) {
////            log.info "famisPropertyById matches: " + this.toString() + famisPropertyById
//            return true
//        }
//
//        return false
//
//    }
//
//    boolean equals(FamisSpace famisSpace, HashMap<String, FamisProperty> famisPropertyById) {
//        if(famisSpace == null) {
//            log.debug "famisSpace is null. Not a match"
//            return false
//        }
//        else if(
//        famisSpace.getFormattedSpaceName(famisPropertyById).equals(this.name)
////                && famisPropertyById.getSupperiorLocation().equals(this.superiorDescriptor)
//        ) {
////            log.info "famisPropertyById matches: " + this.toString() + famisPropertyById
//            return true
//        }
//
//        return false
//
//    }

    public String[] getFields() {
        Map data = asMap()
        return data.keySet().toArray(new String[0])
//        return this.class.declaredFields.toList().toArray(new String[0])
    }

    public updateAuditLog(String message) {
        Date now = new Date()
        auditLog.add(now.toString() + " " + message)

    }

    public resetTracking() {
        isDirty = false // On first load from WD it can't be dirty
        auditLog = new ArrayList<>()

        isNew = false
        isUpdated = false
        hasDuplicate = false
    }


    @Override
    public String toString() {
        return "WDLocation{" +
                "\n\tdescriptor='" + descriptor + '\'' +
                ", \n\twid='" + wid + '\'' +
                ", \n\tid='" + id + '\'' +
                ", \n\tname='" + name + '\'' +
                ", \n\tpreviousName='" + previousName + '\'' +
                ", \n\ttypeId='" + typeId + '\'' +
                ", \n\ttypeWid='" + typeWid + '\'' +
                ", \n\ttypeDescriptor='" + typeDescriptor + '\'' +
                ", \n\tsuperiorDescriptor='" + superiorDescriptor + '\'' +
                ", \n\tsuperiorWid='" + superiorWid + '\'' +
                ", \n\tsuperiorId='" + superiorId + '\'' +
                ", \n\tintegrationI='" + integrationI + '\'' +
                ", \n\tintegrationWid='" + integrationWid + '\'' +
                ", \n\tintegrationFAMISId='" + integrationFAMISId + '\'' +
                ", \n\ttimeZoneId='" + timeZoneId + '\'' +
                ", \n\ttimeZoneDescriptor='" + timeZoneDescriptor + '\'' +
                ", \n\ttimeZoneWid='" + timeZoneWid + '\'' +
                ", \n\tcountryISO2='" + countryISO2 + '\'' +
                ", \n\tcountryISO3='" + countryISO3 + '\'' +
                ", \n\tpostalCode='" + postalCode + '\'' +
                ", \n\tcity='" + city + '\'' +
                ", \n\tstate='" + state + '\'' +
                ", \n\tcountryRegionId='" + countryRegionId + '\'' +
                ", \n\tphoneFormatted='" + phoneFormatted + '\'' +
                ", \n\tphoneInternationalCode='" + phoneInternationalCode + '\'' +
                ", \n\tphoneAreaCode='" + phoneAreaCode + '\'' +
                ", \n\tphoneNumber='" + phoneNumber + '\'' +
                ", \n\tphoneType='" + phoneType + '\'' +
                ", \n\tinactive=" + inactive +
                ", \n\tlatitude=" + latitude +
                ", \n\tlongitude=" + longitude +
                ", \n\taltitude=" + altitude +
                ", \n\tisAddressUpdated=" + isAddressUpdated +
                ", \n\tisPhoneUpdated=" + isPhoneUpdated +
                ", \n\tisDirty=" + isDirty +
                ", \n\tisNew=" + isNew +
                ", \n\tisUpdated=" + isUpdated +
                ", \n\thasDuplicate=" + hasDuplicate +
                ", \n\tauditLog=" + auditLog +
                ", \n\tsuperiorLocation=" + superiorLocation.toString().replace("\n", "").replace("\t", "") +
                '\n}';
    }


    public String toStringTable() {
        return "WDLocation{" +
                ", wid='" + wid + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", superiorDescriptor='" + superiorDescriptor + '\'' +
                ", integrationFAMISId='" + integrationFAMISId + '\'' +
                ", isDirty=" + isDirty +
                ", isNew=" + isNew +
                ", isUpdated=" + isUpdated +
                '}';
    }
}
