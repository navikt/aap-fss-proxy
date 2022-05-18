package no.nav.aap.proxy.organisasjon

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrganisasjonDTO(val navn : OrganisasjonNavnDTO) {
    val fulltNavn =  with(navn) {
        listOfNotNull(navnelinje1,navnelinje2,navnelinje3,navnelinje4,navnelinje5).joinToString(" ")
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class OrganisasjonNavnDTO(val navnelinje1: String?,val navnelinje2: String?,val navnelinje3: String?,val navnelinje4: String?,val navnelinje5: String?)
}