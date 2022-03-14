package no.nav.aap.proxy.inntektskomponent

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.proxy.joark.JoarkWebClientAdapter
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class InntektskomponentClientBeanConfig(
    @Value("\${spring.application.name}") val applicationName: String,
    val cfg: InntektskomponentConfig
) {

    @Bean
    @Qualifier("INNTEKTSKOMPONENT")
    fun joarkWebClient(builder: WebClient.Builder, stsExchangeFilterFunction: ExchangeFilterFunction) =
        builder
            .baseUrl(cfg.baseUri.toString())
            .filter(AbstractWebClientAdapter.correlatingFilterFunction(applicationName))
            .filter(stsExchangeFilterFunction)
            .build()

    @Bean
    fun joarkHealthIndicator(a: JoarkWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {
    }
}