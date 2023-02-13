package no.nav.aap.proxy.arena

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.util.Constants.AAD
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@ProtectedRestController(value = ["/arena"], issuer = AAD, claimMap =[""])
class ArenaController(private val arena: ArenaClient) {
    @GetMapping("/vedtak/{fnr}")
    fun sisteVedtak(@PathVariable fnr: Fødselsnummer) = arena.sisteVedtak(fnr)

    @GetMapping("/haraktivsak")
    fun harAktivSak(@PathVariable fnr: Fødselsnummer) = arena.harAktivSak(fnr)
}