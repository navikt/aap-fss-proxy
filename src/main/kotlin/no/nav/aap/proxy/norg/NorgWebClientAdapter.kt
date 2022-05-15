package no.nav.aap.proxy.norg

import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.Constants
import no.nav.aap.util.Constants.NORG
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class NorgWebClientAdapter( @Qualifier(NORG) client: WebClient, config: NorgConfig) : AbstractWebClientAdapter(client, config) {
    fun hentArbeidsfordeling(request: ArbeidRequest) =
        webClient
            .post()
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono<ArbeidResponse>()
            .doOnError { t: Throwable -> log.warn("Norg feilet", t) }
            .doOnSuccess { log.trace("Norg OK") }
            .block()
            .also { log.trace("Norg response $it") }
}