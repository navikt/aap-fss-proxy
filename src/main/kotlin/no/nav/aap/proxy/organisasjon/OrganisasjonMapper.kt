package no.nav.aap.proxy.organisasjon

internal object OrganisasjonMapper {
    private const val NAVN = "navn"
    private const val NAVNELINJE1 = "navnelinje1"
    private const val NAVNELINJE2 = "navnelinje2"
    private const val NAVNELINJE3 = "navnelinje3"
    private const val NAVNELINJE4 = "navnelinje4"
    private const val NAVNELINJE5 = "navnelinje5"

    fun tilOrganisasjonsnavn(respons: Map<String, Map<String,String>>): String {
            val navn = respons[NAVN]
            return listOfNotNull(getit(navn, NAVNELINJE1),
                    getit(navn, NAVNELINJE2),
                    getit(navn, NAVNELINJE3),
                    getit(navn, NAVNELINJE4),
                    getit(navn, NAVNELINJE5)).joinToString { ", " }
    }
     private fun getit(m: Map<String, String>?, key: String) = m?.map { m[key]  }
}