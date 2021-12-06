package no.nav.aap.proxy.joark

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.proxy.sts.StsClient
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.StringExtensions.asBearer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

@Configuration
class JoarkClientBeanConfig(@Value("\${spring.application.name}") val applicationName: String,  val cfg: JoarkConfig) {

    @Bean
    @Qualifier(JOARK)
    fun joarkWebClient(stsClient: StsClient,builder: WebClient.Builder) =
        builder
            .baseUrl(cfg.baseUri.toString())
            .filter(correlatingFilterFunction(applicationName))
            .filter(stsExchangeFilterFunction(stsClient))
            .build()

    @Bean
    fun joarkHealthIndicator(a: JoarkWebClientAdapter) = object : AbstractPingableHealthIndicator(a){

    }
    private fun stsExchangeFilterFunction(stsClient: StsClient) =
        ExchangeFilterFunction { req, next -> next.exchange(ClientRequest.from(req).header(AUTHORIZATION, "${stsClient.oidcToken().asBearer()}").build()) }
}