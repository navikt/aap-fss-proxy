package no.nav.aap.proxy.inntektskomponent

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.proxy.joark.JoarkWebClientAdapter
import no.nav.aap.proxy.sts.StsClient
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.StringExtensions.asBearer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class InntektskomponentClientBeanConfig(
    @Value("\${spring.application.name}") val applicationName: String,
    val cfg: InntektskomponentConfig
) {

    @Bean
    @Qualifier("INNTEKTSKOMPONENT")
    fun joarkWebClient(stsClient: StsClient, builder: WebClient.Builder) =
        builder
            .baseUrl(cfg.baseUri.toString())
            .filter(AbstractWebClientAdapter.correlatingFilterFunction(applicationName))
            .filter(stsExchangeFilterFunction(stsClient))
            .build()

    @Bean
    fun joarkHealthIndicator(a: JoarkWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {
    }

    private fun stsExchangeFilterFunction(stsClient: StsClient) =
        ExchangeFilterFunction { req, next ->
            next.exchange(
                ClientRequest.from(req).header(HttpHeaders.AUTHORIZATION, "${stsClient.oidcToken().asBearer()}").build()
            )
        }
}