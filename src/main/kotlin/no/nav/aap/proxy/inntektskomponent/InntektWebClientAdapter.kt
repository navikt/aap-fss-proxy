package no.nav.aap.proxy.inntektskomponent

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.Constants.INNTEKTSKOMPONENT
import no.nav.aap.util.WebClientExtensions.response

@Component
class InntektWebClientAdapter(@Qualifier(INNTEKTSKOMPONENT) webClient : WebClient, private val cf : InntektConfig) : AbstractWebClientAdapter(webClient, cf) {

    fun getInntekt(request : InntektRequest) =
        webClient
            .post()
            .uri { b -> b.path(cf.path).build() }
            .contentType(APPLICATION_JSON)
            .bodyValue(request)
            .exchangeToMono { it.response<InntektResponse>() }
            .doOnError { t : Throwable -> log.warn("Inntektsoppslag feilet", t) }
            .doOnSuccess { log.trace("Inntektsoppslag OK") }
            .retryWhen(cf.retrySpec(log))
            .contextCapture()
            .block() ?: throw IrrecoverableIntegrationException("Null reponse fra inntekt")
}