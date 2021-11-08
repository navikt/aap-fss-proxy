package no.nav.aap.joark

import no.nav.aap.sts.StsClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.*
import reactor.core.publisher.Mono

@Configuration
class JoarkClientConfig(
    private val webClientBuilder: WebClient.Builder,
    private val joarkConfig: JoarkConfig
) {
    @Bean
    fun joarkWebClient(stsClient: StsClient): WebClient {
        return webClientBuilder
            .baseUrl(joarkConfig.url)
            .filter(stsExchangeFilterFunction(stsClient))
            .build()
    }

    private fun stsExchangeFilterFunction(stsClient: StsClient) =
        ExchangeFilterFunction { req, next -> next.exchange(ClientRequest.from(req).header(HttpHeaders.AUTHORIZATION, "Bearer ${stsClient.oidcToken()}").build()) }
}