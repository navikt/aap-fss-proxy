package no.nav.aap.proxy.organisasjon

data class OrganisasjonDTO(val navn : OrganisasjonNavnDTO) {
    val fulltNavn =  listOfNotNull(navn.navnelinje1,navn.navnelinje2,navn.navnelinje3,navn.navnelinje4,navn.navnelinje5).joinToString { ", " }
    data class OrganisasjonNavnDTO(val navnelinje1: String ?,val navnelinje2: String ?,val navnelinje3: String ?,val navnelinje4: String ?,val navnelinje5: String ?)
}