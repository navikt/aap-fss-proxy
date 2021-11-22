package no.nav.aap.joark

import no.nav.aap.util.MDCUtil
import no.nav.aap.sts.StsClient
import no.nav.boot.conditionals.ConditionalOnDevOrLocal
import org.springframework.boot.actuate.trace.http.HttpTraceRepository
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import no.nav.aap.rest.AbstractRestConfig.Companion.correlatingFilterFunction
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class JoarkClientConfig(private val webClientBuilder: WebClient.Builder, private val cfg: JoarkConfig) {
    @Bean
    fun joarkWebClient(stsClient: StsClient): WebClient {
        return webClientBuilder
            .baseUrl(cfg.baseUri.toString())
            .filter(correlatingFilterFunction())
            .filter(stsExchangeFilterFunction(stsClient))
            .build()
    }

    @Bean
    @ConditionalOnDevOrLocal
    fun httpTraceRepository(): HttpTraceRepository = InMemoryHttpTraceRepository()

    private fun stsExchangeFilterFunction(stsClient: StsClient) =
        ExchangeFilterFunction { req, next -> next.exchange(ClientRequest.from(req).header(AUTHORIZATION, "Bearer ${stsClient.oidcToken()}").build()) }
}