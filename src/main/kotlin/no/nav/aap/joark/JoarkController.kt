package no.nav.aap.joark

import no.nav.aap.config.Constants.ISSUER_AAD
import no.nav.aap.joark.domain.Journalpost
import no.nav.security.token.support.spring.ProtectedRestController
import org.slf4j.LoggerFactory.getLogger
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody


@ProtectedRestController(value = ["/joark"], issuer = ISSUER_AAD, claimMap =[""] )
class JoarkController(private val joark: JoarkClient) {

    private val log = getLogger(javaClass)

    @PostMapping("/aad")
    fun opprettJournalpostFraSaksbehandler(@RequestBody journalpost: Journalpost) {
        log.info("oppretter journalpost $journalpost")
        joark.opprettJournalpost(journalpost)
    }
}