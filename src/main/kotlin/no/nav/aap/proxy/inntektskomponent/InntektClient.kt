package no.nav.aap.proxy.inntektskomponent

import org.springframework.stereotype.Component

@Component
class InntektClient(private val adapter: InntektWebClientAdapter) {
    fun getInntekt(request: InntektRequest) = adapter.getInntekt(request)
}