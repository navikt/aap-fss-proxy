package no.nav.aap.proxy.joark

import no.nav.aap.health.AbstractPingableHealthIndicator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.Constants.STS

@Configuration
class JoarkClientBeanConfig(val cfg: JoarkConfig) {

    @Bean
    @Qualifier(JOARK)
    fun joarkWebClient(builder: WebClient.Builder, @Qualifier(STS) stsExchangeFilterFunction: ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(stsExchangeFilterFunction)
            .build()

    @Bean
    fun joarkHealthIndicator(a: JoarkWebClientAdapter) = object : AbstractPingableHealthIndicator(a){}

}