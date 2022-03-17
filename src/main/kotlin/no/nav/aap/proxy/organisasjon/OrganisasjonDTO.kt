package no.nav.aap.proxy.organisasjon

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrganisasjonDTO(val navn : OrganisasjonNavnDTO) {
    fun fulltNavn() =  listOfNotNull(navn.navnelinje1,navn.navnelinje2,navn.navnelinje3,navn.navnelinje4,navn.navnelinje5).joinToString { ", " }
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class OrganisasjonNavnDTO(val navnelinje1: String ?,val navnelinje2: String ?,val navnelinje3: String ?,val navnelinje4: String ?,val navnelinje5: String ?)
}