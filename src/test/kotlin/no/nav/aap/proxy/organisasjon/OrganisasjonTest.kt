package no.nav.aap.proxy.organisasjon

import no.nav.aap.proxy.organisasjon.OrganisasjonDTO.OrganisasjonNavnDTO
import org.junit.jupiter.api.Test

class OrganisasjonTest {
    @Test
    fun testSerialize() {
        val navn = OrganisasjonNavnDTO("1", "2", "3", "4", "5")
        var o = OrganisasjonDTO(navn)
        print(listOfNotNull(navn.navnelinje1,navn.navnelinje2,navn.navnelinje3,navn.navnelinje4,navn.navnelinje5).joinToString(" "))

    }
}