package no.nav.aap.proxy.joark

import no.nav.aap.api.felles.OrgNummer
import no.nav.aap.joark.Journalpost
import no.nav.aap.proxy.organisasjon.OrganisasjonController
import no.nav.aap.proxy.organisasjon.OrganisasjonController.Companion
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.core.api.Unprotected
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody


@ProtectedRestController(value = ["/joark"], issuer = IDPORTEN)
class JoarkController(private val joark: JoarkClient) {

    @PostMapping("/opprett")
    fun opprettJournalpost(@RequestBody p: Journalpost) = joark.opprettJournalpost(p)
    @GetMapping("/ping")
    @Unprotected
    fun ping() = joark.ping()
}