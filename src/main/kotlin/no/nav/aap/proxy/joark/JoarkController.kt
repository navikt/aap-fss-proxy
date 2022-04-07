package no.nav.aap.proxy.joark

import no.nav.aap.joark.JoarkResponse
import no.nav.aap.joark.Journalpost
import no.nav.aap.util.Constants
import no.nav.aap.util.Constants.AAD
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.spring.ProtectedRestController
import org.slf4j.LoggerFactory.getLogger
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody


@ProtectedRestController(value = ["/joark"], issuer = IDPORTEN)
class JoarkController(private val joark: JoarkClient) {

    @PostMapping("/aad")
    fun opprettJournalpostFraSaksbehandler(@RequestBody journalpost: Journalpost) = joark.opprettJournalpost(journalpost)
}