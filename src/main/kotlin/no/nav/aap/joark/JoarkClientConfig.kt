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
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class JoarkClientConfig(private val webClientBuilder: WebClient.Builder, private val joarkConfig: JoarkConfig) {
    @Bean
    fun joarkWebClient(stsClient: StsClient): WebClient {
        return webClientBuilder
            .baseUrl(joarkConfig.url)
            .filter(correlatingFilterFunction())
            .filter(stsExchangeFilterFunction(stsClient))
            .build()
    }

    @Bean
    @ConditionalOnDevOrLocal
    fun httpTraceRepository(): HttpTraceRepository {
        return InMemoryHttpTraceRepository()
    }

    private fun correlatingFilterFunction() =
         ExchangeFilterFunction { req: ClientRequest, next: ExchangeFunction ->
            next.exchange(
                ClientRequest.from(req)
                    .header(MDCUtil.NAV_CONSUMER_ID, MDCUtil.consumerId())
                    .header(MDCUtil.NAV_CALL_ID2, MDCUtil.callId())
                    .build()
            )
        }

    private fun stsExchangeFilterFunction(stsClient: StsClient) =
        ExchangeFilterFunction { req, next -> next.exchange(ClientRequest.from(req).header(AUTHORIZATION, "Bearer ${stsClient.oidcToken()}").build()) }
}