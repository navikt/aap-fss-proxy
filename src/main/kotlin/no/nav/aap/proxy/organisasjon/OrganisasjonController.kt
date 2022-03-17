package no.nav.aap.proxy.organisasjon

import no.nav.aap.util.Constants.IDPORTEN
import no.nav.aap.util.LoggerUtil
import no.nav.security.token.support.core.api.Unprotected
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam


@ProtectedRestController(value = ["/organisasjon"], issuer = IDPORTEN)
class OrganisasjonController(val orgClient: OrganisasjonClient) {

    private  val NAV = "998004993"
    private val log = LoggerUtil.getLogger(javaClass)


    @GetMapping
    fun navn(@RequestParam("orgnummer") orgnummer: String): String {
        log.info("Henter orgnavn for $orgnummer")
        return  orgClient.orgNavn(orgnummer)
    }

    @GetMapping("/ping")
    @Unprotected
    fun ping() = orgClient.orgNavn(NAV)
}