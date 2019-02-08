package net.heithoff.base

import workday.com.bsvc.CountryObjectIDType
import workday.com.bsvc.CountryObjectType

class CountryReference {
    CountryObjectType countryObjectType
    String wid

    CountryReference(CountryObjectType countryObjectType) {
        this.countryObjectType = countryObjectType
        List<CountryObjectIDType> ids = countryObjectType.ID
        wid = ids.find {it.type == "WID"}.value
    }
}
