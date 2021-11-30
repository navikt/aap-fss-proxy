package no.nav.aap.proxy.joark

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.proxy.sts.StsClient
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.boot.conditionals.ConditionalOnDevOrLocal
import org.springframework.boot.actuate.trace.http.HttpTraceRepository
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.util.Constants.JOARK
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Configuration
class JoarkClientBeanConfig(private val webClientBuilder: WebClient.Builder, private val cfg: JoarkConfig) {
    @Value("\${spring.application.name}")
    private lateinit var applicationName: String

    @Bean
    @Qualifier(JOARK)
    fun joarkWebClient(stsClient: StsClient): WebClient {
        return webClientBuilder
            .baseUrl(cfg.baseUri.toString())
            .filter(correlatingFilterFunction(applicationName))
            .filter(stsExchangeFilterFunction(stsClient))
            .build()
    }

    @Component
    class JoarkHealthIndicator(adapter: JoarkWebClientAdapter) : AbstractPingableHealthIndicator(adapter)

    private fun stsExchangeFilterFunction(stsClient: StsClient) =
        ExchangeFilterFunction { req, next -> next.exchange(ClientRequest.from(req).header(AUTHORIZATION, "Bearer ${stsClient.oidcToken()}").build()) }
}