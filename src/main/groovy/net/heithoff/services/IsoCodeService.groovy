package net.heithoff.services

/**
 * Created by jason on 2/15/16.
 */
class IsoCodeService {
    private static def isoCodesMap

    IsoCodeService() throws Exception {
        //initialization logic goes here
        initIsoCodes()
    }

    def iso3CountryCodeToIso2CountryCode(String iso3CountryCode) {
        // e.g. DEU -> DE, AUT -> AT, AFG -> AF, ....
        return isoCodesMap.(iso3CountryCode.toUpperCase())
    }

    def iso2CountryCodeToIso3CountryCode(String iso2CountryCode){
        def locale = new Locale("", iso2CountryCode);
        return locale.getISO3Country();
    }

    def iso2CountryCodeToCountryName(String iso2CountryCode) {
        return isoCountryCodeToCountryName(iso2CountryCode)
    }

    def iso3CountryCodeToCountryName(String iso3CountryCode) {
        def iso2CountryCode = isoCodesMap.(iso3CountryCode.toUpperCase())
        return isoCountryCodeToCountryName(iso2CountryCode)
    }

    private def isoCountryCodeToCountryName(String isoCountryCode) {
        def locale = new Locale("", isoCountryCode);
        return locale.getDisplayCountry()
    }

    private def initIsoCodes () {
        if(!isoCodesMap) {
            isoCodesMap = [:]

            Locale.getISOCountries().each { country ->
                def locale = new Locale("", country)
                // e.g. isoCodesMap = [DEU:DE, AUT:AT, AFG:AF, ATG:AG, AIA:AI, ALB:AL, ARM:AM, ANT:AN,....
                isoCodesMap.put(locale.getISO3Country().toUpperCase(), country)
            }
//            log.info "isoCodesMap: ${isoCodesMap}"
        }
    }
}
