package no.nav.aap.proxy.inntektskomponent

import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import no.nav.aap.util.Constants.INNTEKTSKOMPONENT
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class InntektskomponentWebClientAdapter(
    @Qualifier(INNTEKTSKOMPONENT) webClient: WebClient,
    private val cf: InntektskomponentConfig
) :
    AbstractWebClientAdapter(webClient, cf) {
    fun getInntekt(request: InntektskomponentRequest) =
        webClient
            .post()
            .uri { b -> b.path(cf.path).build() }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .onStatus({ obj: HttpStatus -> obj.isError }) { obj: ClientResponse -> obj.createException() }
            .bodyToMono<InntektskomponentResponse>()
            .block()
}