package no.nav.aap.proxy.arena

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.*
import javax.inject.Inject
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion


@Component
class ArenaOIDCClient (private val a: ArenaOIDCWebClientAdapter)  {
    fun oidcToken() = a.oidcToken()
}