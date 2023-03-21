package no.nav.aap.proxy.inntektskomponent

import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.Constants.INNTEKTSKOMPONENT
import no.nav.aap.util.WebClientExtensions.toResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class InntektWebClientAdapter(@Qualifier(INNTEKTSKOMPONENT) webClient: WebClient, private val cf: InntektConfig) : AbstractWebClientAdapter(webClient, cf) {

    fun getInntekt(request: InntektRequest) =
        webClient
            .post()
            .uri { b -> b.path(cf.path).build() }
            .contentType(APPLICATION_JSON)
            .bodyValue(request)
            .exchangeToMono { it.toResponse<InntektResponse>(log)}
            .retryWhen(cf.retrySpec(log))
            .doOnError { t: Throwable -> log.warn("Inntektsoppslag feilet", t) }
            .doOnSuccess { log.trace("Inntektsoppslag OK") }
            .block()
            .also { log.trace("Inntekt response $it") }
}