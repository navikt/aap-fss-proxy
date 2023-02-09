package no.nav.aap.proxy.arena

import org.springframework.stereotype.Component
<<<<<<< HEAD
=======
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.*
import javax.inject.Inject
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT

@ConfigurationProperties("arenaoidc")
class ArenaOIDCConfig(baseUri: URI,
                      @DefaultValue("true") enabled: Boolean, retry: RetryConfig = DEFAULT): AbstractRestConfig(baseUri,"",
    "arenaoidc",enabled, retry)

@ConfigurationProperties("arenaclient")
data class ArenauserConfig (val id: String, val secret: String) {
    val credentials = Base64.getEncoder().encodeToString("$id:$secret".toByteArray(StandardCharsets.UTF_8))
}

@Configuration
class ArenaOIDCClientConfig(private val cfg: ArenauserConfig) {

    @Bean
    @Qualifier("arenaoidc")
    fun arenaOIDCWebClient(builder: WebClient.Builder, arenaOIDCConfig: ArenaOIDCConfig) =
        builder
            .baseUrl("${arenaOIDCConfig.baseUri}")
            .filter(arenaOIDCExchangeFilterFunction())
            .build()

    @Bean
    fun arenaOIDCHealthIndicator(a: StsWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}

    private fun arenaOIDCExchangeFilterFunction() =
        ExchangeFilterFunction {
                req, next -> next.exchange(
            ClientRequest.from(req).header(HttpHeaders.AUTHORIZATION, "Basic ${cfg.credentials}")
            .build())
        }
}
>>>>>>> 4c5fbbb (janoalav gjør ting han ikke kan)

@Component
class ArenaOIDCClient (private val a: ArenaOIDCWebClientAdapter)  {
    fun oidcToken() = a.oidcToken()
}