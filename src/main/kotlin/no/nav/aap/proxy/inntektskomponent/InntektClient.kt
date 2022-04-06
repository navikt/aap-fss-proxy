package no.nav.aap.proxy.inntektskomponent

import org.springframework.stereotype.Component

@Component
class InntektskomponentClient(private val adapter: InntektskomponentWebClientAdapter) {
    fun getInntekt(request: InntektskomponentRequest) = adapter.getInntekt(request)
}