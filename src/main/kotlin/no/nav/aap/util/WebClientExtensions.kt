package no.nav.aap.util

import org.springframework.web.reactive.function.client.ClientResponse
import reactor.core.publisher.Mono
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.api.felles.error.RecoverableIntegrationException

object WebClientExtensions {

    inline fun <reified T : Any> ClientResponse.response() =
        with(statusCode().value()) {
            if (this in 200..299) {
                bodyToMono(T::class.java)
            }
            else if (this in 400..499) {
                Mono.error(IrrecoverableIntegrationException("HTTP $this"))
            }
            else {
                Mono.error(RecoverableIntegrationException("HTTP $this"))
            }
        }
}