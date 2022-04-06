package no.nav.aap.proxy.inntektskomponent

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.util.Constants.INNTEKTSKOMPONENT
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.consumerFilterFunction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class InntektClientBeanConfig(val cfg: InntektConfig) {

    @Bean
    @Qualifier(INNTEKTSKOMPONENT)
    fun inntektWebClient(builder: WebClient.Builder, stsExchangeFilterFunction: ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(consumerFilterFunction())
            .filter(stsExchangeFilterFunction)
            .build()

    @Bean
    fun inntektHealthIndicator(a: InntektWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}
}