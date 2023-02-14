package no.nav.aap.proxy.arena

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.util.Constants.AAD
import no.nav.boot.conditionals.ConditionalOnDev
import no.nav.security.token.support.core.api.Unprotected
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@ConditionalOnDev
class ArenaDevController(private val arena: ArenaClient) {
    @GetMapping("/dev/vedtak/{fnr}")
    fun sisteVedtak(@PathVariable fnr: Fødselsnummer) = arena.sisteVedtak(fnr)

    @GetMapping("/dev/haraktivsak/{fnr}")
    @Unprotected
    fun harAktivSak(@PathVariable fnr: Fødselsnummer) = arena.harAktivSak(fnr)
}