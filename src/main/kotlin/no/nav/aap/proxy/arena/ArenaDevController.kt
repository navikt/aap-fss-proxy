package no.nav.aap.proxy.arena

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.util.Constants.AAD
import no.nav.boot.conditionals.ConditionalOnDev
import no.nav.security.token.support.core.api.Unprotected
import no.nav.security.token.support.spring.ProtectedRestController
import no.nav.security.token.support.spring.UnprotectedRestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@UnprotectedRestController(value = ["/dev"])
class ArenaDevController(private val arena: ArenaClient) {
    @GetMapping("vedtak/{fnr}")
    fun sisteVedtak(@PathVariable fnr: Fødselsnummer) = arena.sisteVedtak(fnr)

    @GetMapping("/haraktivsak/{fnr}")
    @Unprotected
    fun harAktivSak(@PathVariable fnr: Fødselsnummer) = arena.harAktivSak(fnr)

    @PostMapping("/opprettsak")
    @Unprotected
    fun opprettOppgave( @RequestBody opgaveInfo: ArenaOpprettOppgaveParams) = arena.opprettOppgave(opgaveInfo)
}