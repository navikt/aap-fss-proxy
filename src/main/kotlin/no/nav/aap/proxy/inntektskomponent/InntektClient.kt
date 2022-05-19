package no.nav.aap.proxy.inntektskomponent

import org.springframework.stereotype.Component

@Component
class InntektClient(private val a: InntektWebClientAdapter) {
    fun getInntekt(request: InntektRequest) = a.getInntekt(request)
}