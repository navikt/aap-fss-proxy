package no.nav.aap.proxy.arena

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.proxy.arena.ArenaOIDCConfig.Companion.ARENAOIDC
import no.nav.aap.proxy.sts.StsWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.*
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class ArenaOIDCClientBeanConfig(private val cfg: ArenaUserConfig) {

    @Bean
    @Qualifier(ARENAOIDC)
    fun arenaOIDCWebClient(builder: Builder, cfg: ArenaOIDCConfig) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(arenaOIDCExchangeFilterFunction())
            .build()

    @Bean
    fun arenaOIDCHealthIndicator(a: StsWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}

    private fun arenaOIDCExchangeFilterFunction() =
        ExchangeFilterFunction { req, next -> next.exchange(ClientRequest.from(req)
            .header(AUTHORIZATION, "Basic ${cfg.credentials}").build()) }
}