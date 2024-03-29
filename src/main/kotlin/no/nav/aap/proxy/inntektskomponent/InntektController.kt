package no.nav.aap.proxy.inntektskomponent

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import no.nav.aap.util.Constants.AAD
import no.nav.security.token.support.spring.ProtectedRestController

@ProtectedRestController(value = ["/inntektskomponent"], issuer = AAD, claimMap = [""])
class InntektController(private val inntekt : InntektClient) {

    @PostMapping("/")
    fun getInntekt(@RequestBody req : InntektRequest) = inntekt.getInntekt(req)
}