package no.nav.aap.joark

import no.nav.aap.sts.StsClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class JoarkClientConfig(private val webClientBuilder: WebClient.Builder, private val joarkConfig: JoarkConfig) {
    @Bean
    fun joarkWebClient(stsClient: StsClient): WebClient {
        return webClientBuilder
            .baseUrl(joarkConfig.url)
            .filter(stsExchangeFilterFunction(stsClient))
            .build()
    }

    private fun stsExchangeFilterFunction(stsClient: StsClient) =
        ExchangeFilterFunction { req, next -> next.exchange(ClientRequest.from(req).header(AUTHORIZATION, "Bearer ${stsClient.oidcToken()}").build()) }
}