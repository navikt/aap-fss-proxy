package no.nav.aap.proxy.organisasjon

import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam


@ProtectedRestController(value = ["/organisasjon"], issuer = IDPORTEN)
class OrganisasjonController(val orgClient: OrganisasjonClient) {

    @GetMapping
    fun navn(@RequestParam("orgnummer") orgnummer: String?) =
         orgClient.orgNavn(orgnummer)
}