package no.nav.aap.proxy.arena

import no.nav.aap.proxy.inntektskomponent.InntektClient
import no.nav.aap.proxy.inntektskomponent.InntektRequest
import no.nav.aap.util.Constants
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@ProtectedRestController(value = ["/arena"], issuer = Constants.AAD, claimMap =[""])
class ArenaController(private val arena: ArenaClient) {
    @GetMapping("/vedtak/{fnr}")
    fun getSisteVedtak(@PathVariable fnr: String) = arena.getSisteVedtak(fnr)
}