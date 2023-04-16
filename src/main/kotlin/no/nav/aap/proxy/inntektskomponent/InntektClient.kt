package no.nav.aap.proxy.inntektskomponent

import io.micrometer.observation.annotation.Observed
import org.springframework.stereotype.Component

@Component
@Observed
class InntektClient(private val a : InntektWebClientAdapter) {

    fun getInntekt(request : InntektRequest) = a.getInntekt(request)
}