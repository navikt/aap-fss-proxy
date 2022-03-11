package no.nav.aap.proxy.arbeidsforhold

import no.nav.aap.util.Constants.AAD
import no.nav.aap.util.LoggerUtil
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.GetMapping


@ProtectedRestController(value = ["/arbeidsforhold"], issuer = AAD, claimMap =[""])
class ArbeidsforholdController(private val arbeid: ArbeidsforholdClient) {

    private val log = LoggerUtil.getLogger(javaClass)


    @GetMapping
    fun opprettJournalpostFraSaksbehandler(): MutableList<Map<*, *>>? {
          log.info("Henter arbeidsforhold")
          return arbeid.arbeidsforhold()
    }
}