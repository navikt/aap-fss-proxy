package no.nav.aap.util

import org.springframework.web.reactive.function.client.ClientResponse
import reactor.core.publisher.Mono
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.api.felles.error.RecoverableIntegrationException

object WebClientExtensions {

    inline fun <reified T> ClientResponse.response() =
        with(statusCode()) {
            if (is2xxSuccessful) {
                bodyToMono(T::class.java)
            }
            else if (is4xxClientError) {
                Mono.error(IrrecoverableIntegrationException("$this"))
            }
            else {
                Mono.error(RecoverableIntegrationException("$this"))
            }
        }
}