package no.nav.aap.proxy.arena

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.proxy.arena.ArenaConfig.Companion.ARENA
import no.nav.aap.proxy.sts.StsWebClientAdapter
import no.nav.aap.util.StringExtensions.asBearer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.*
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class ArenaClientBeanConfig(private val cfg: ArenaConfig) {

    @Bean
    @Qualifier(ARENA)
    fun arenaWebClient(builder: Builder, arenaOIDCClient: ArenaOIDCClient) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(arenaOIDCExchangeFilterFunction(arenaOIDCClient))
            .build()

    private fun arenaOIDCExchangeFilterFunction(arenaOIDCClient: ArenaOIDCClient) =
        ExchangeFilterFunction {
            req, next -> next.exchange(ClientRequest.from(req)
            .header(AUTHORIZATION, "${arenaOIDCClient.oidcToken().asBearer()}")
            .build())
        }


    @Bean
    fun arenaHealthIndicator(a: StsWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}

}