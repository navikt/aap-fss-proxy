package no.nav.aap.joark.controller

import no.nav.aap.config.SecurityConfig.Companion.ISSUER_AAD
import no.nav.aap.config.SecurityConfig.Companion.ISSUER_IDPORTEN
import no.nav.aap.joark.client.JoarkClient
import no.nav.aap.joark.domain.Journalpost
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController("/joark")
class JoarkController(
    private val joarkClient: JoarkClient
) {

    @ProtectedWithClaims(issuer = ISSUER_IDPORTEN)
    @PostMapping("/idporten")
    fun opprettJournalpostFraBruker(@RequestBody journalpost: Journalpost) {
        joarkClient.opprettJournalpost(journalpost)
    }

    @ProtectedWithClaims(issuer = ISSUER_AAD)
    @PostMapping("/aad")
    fun opprettJournalpostFraSaksbehandler(@RequestBody journalpost: Journalpost) {
        joarkClient.opprettJournalpost(journalpost)
    }

}
