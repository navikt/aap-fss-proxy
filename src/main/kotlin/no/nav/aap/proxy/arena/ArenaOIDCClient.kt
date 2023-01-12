package no.nav.aap.proxy.arena

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.proxy.sts.StsWebClientAdapter
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.*
import javax.inject.Inject

@ConfigurationProperties("arenaoidc")
class ArenaOIDCConfig (baseUri: URI,
                       @DefaultValue("true") enabled: Boolean): AbstractRestConfig(baseUri,"",
    "arenaoidc",enabled)

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

@Component
class ArenaOIDCWebClientAdapter(@Qualifier("arenaoidc") webClient: WebClient, cf: ArenaOIDCConfig) :
    AbstractWebClientAdapter(webClient, cf) {

    var token  = getTheToken()

    fun oidcToken(): String {
        if (token.hasExpired()) {
            log.trace("Fornyer token")
            token = getTheToken()
        }
        return token.accessToken!!
    }

    private fun getTheToken() =
        webClient.post()
            .uri { b ->
                b.path("/oauth/token").build()
            }
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("grant_type=client_credentials")
            .retrieve()
            .bodyToMono<ArenaOidcToken>()
            .doOnError { t: Throwable -> log.warn("Arena OIDC oppslag feilet!", t) }
            .doOnSuccess { log.trace("Arena OIDC oppslag OK, utgår om ${it.expiresIn}s") }
            .block() ?: throw IllegalStateException("Ingen respons fra Arena OIDC")

    override fun ping() = mapOf("status" to "OK")  // TODO hvordan pinge denne
}

@Component
class ArenaOIDCClient @Inject constructor(private val a: ArenaOIDCWebClientAdapter)  {
    constructor(webClient: WebClient, cfg: ArenaOIDCConfig) : this(ArenaOIDCWebClientAdapter(webClient,cfg))
    fun oidcToken() = a.oidcToken()
}