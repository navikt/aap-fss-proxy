package no.nav.aap.proxy.arena.rest

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.proxy.arena.rest.ArenaRestConfig.Companion.ARENA
import no.nav.aap.proxy.arena.rest.ArenaRestConfig.Companion.ARENAOIDC
import no.nav.aap.proxy.sts.StsWebClientAdapter
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.StringExtensions.asBearer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.*
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class ArenaRestBeanConfig {

    private val log = LoggerUtil.getLogger(javaClass)


    @Bean
    @Qualifier(ARENAOIDC)
    fun arenaOIDCWebClient(builder: Builder, cfg: ArenaRestConfig, @Qualifier(ARENAOIDC) filter: ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(filter)
            .build()

    @Bean
    @Qualifier(ARENAOIDC)
    fun arenaOIDCExchangeFilterFunction(cfg: ArenaRestConfig) =
        ExchangeFilterFunction {
            req, next -> next.exchange(
                ClientRequest.from(req).header(AUTHORIZATION, cfg.asBasic)
                    .build())
        }

    @Bean
    @Qualifier(ARENA)
    fun arenaWebClient(builder: Builder, cfg: ArenaRestConfig, @Qualifier(ARENA) arenaExchangeFilterFunction: ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(arenaExchangeFilterFunction)
            .build()

    @Bean
    @Qualifier(ARENA)
    fun arenaExchangeFilterFunction(a: ArenaOIDCWebClientAdapter) =
        ExchangeFilterFunction {
            req, next -> next.exchange(ClientRequest.from(req).header(AUTHORIZATION, a.oidcToken().asBearer())
            .build())
        }

    @Bean
    fun arenaRestHealthIndicator(a: StsWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}

}