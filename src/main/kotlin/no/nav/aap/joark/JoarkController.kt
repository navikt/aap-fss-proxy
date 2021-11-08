package no.nav.aap.joark

import no.nav.aap.config.SecurityConfig.Companion.ISSUER_AAD
import no.nav.aap.joark.JoarkClient
import no.nav.aap.joark.domain.Journalpost
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@ProtectedRestController(value = ["/joark"], issuer = ISSUER_AAD)
class JoarkController(
    private val joarkClient: JoarkClient
) {

    @PostMapping("/aad")
    fun opprettJournalpostFraSaksbehandler(@RequestBody journalpost: Journalpost) {
        joarkClient.opprettJournalpost(journalpost)
    }

}