package no.nav.aap.joark.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class JoarkClientConfig(
    private val webClientBuilder: WebClient.Builder,
    private val joarkConfig: JoarkConfig
) {
    @Bean
    fun joarkWebClient(): WebClient {
        return webClientBuilder
            .baseUrl(joarkConfig.url)
            .build()
    }
}
