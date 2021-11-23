package no.nav.aap.rest

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient

abstract class AbstractWebClientAdapter(protected val webClient: WebClient, protected open val cfg: AbstractRestConfig) : RetryAware, Pingable {
    override fun ping() {
        webClient
            .get()
            .uri(pingEndpoint())
            .accept(APPLICATION_JSON, TEXT_PLAIN)
            .retrieve()
            .onStatus({ obj: HttpStatus -> obj.isError }) { obj: ClientResponse -> obj.createException() }
            .toBodilessEntity()
            .block()
    }

    override fun name() = cfg.name()
    override fun pingEndpoint() = cfg.pingEndpoint()

}