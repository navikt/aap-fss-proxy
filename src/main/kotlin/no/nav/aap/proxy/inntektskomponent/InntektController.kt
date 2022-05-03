package no.nav.aap.proxy.inntektskomponent

import no.nav.aap.util.Constants.AAD
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@ProtectedRestController(value = ["/inntektskomponent"], issuer = AAD, claimMap =[""])
class InntektController(private val inntekt: InntektClient) {
    @PostMapping("/")
    fun getInntekt(@RequestBody request: InntektRequest) = inntekt.getInntekt(request)
}