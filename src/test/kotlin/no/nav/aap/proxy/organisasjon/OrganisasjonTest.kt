package no.nav.aap.proxy.organisasjon

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.aap.proxy.organisasjon.OrganisasjonDTO.OrganisasjonNavnDTO
import org.junit.jupiter.api.Test

class OrganisasjonTest {
    @Test
    fun testSerialize() {
        var o = OrganisasjonDTO(OrganisasjonNavnDTO("1","2","3","4","5"))
        println(ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o))
    }
}