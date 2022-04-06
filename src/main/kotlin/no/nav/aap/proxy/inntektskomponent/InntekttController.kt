package no.nav.aap.proxy.inntektskomponent

import no.nav.aap.util.Constants
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@ProtectedRestController(value = ["/inntektskomponent"], issuer = Constants.AAD, claimMap =[""])
class InntektskomponentController(private val inntektskomponent: InntektClient) {
    @PostMapping("/")
    fun getInntekt(@RequestBody request: InntektskomponentRequest) = inntektskomponent.getInntekt(request)
}