package no.nav.aap.proxy.norg

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.util.Constants
import no.nav.aap.util.Constants.NORG
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class NorgClientBeanConfig(val config: NorgConfig) {
    @Bean
    fun norgHealthIndicator(a: NorgWebClientAdapter) =  object : AbstractPingableHealthIndicator(a){}


    @Bean
    @Qualifier(NORG)
    fun norgClientWebAdapter(builder: Builder, stsExchange: ExchangeFilterFunction): WebClient =
        builder
            .baseUrl("${config.baseUri}")
            .filter(stsExchange).build()
}