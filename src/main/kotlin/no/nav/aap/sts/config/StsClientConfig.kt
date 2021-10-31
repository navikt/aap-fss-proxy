package no.nav.aap.sts.config

import no.nav.aap.config.ServiceuserConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import java.util.*

@Configuration
class StsClientConfig(
    private val webClientBuilder: WebClient.Builder,
    private val stsConfig: StsConfig,
    private val serviceuserConfig: ServiceuserConfig
) {

    @Bean
    fun stsWebClient(): WebClient {
        return webClientBuilder
            .baseUrl("${stsConfig.url}/rest/v1/sts/token")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic ${credentials()}")
            .build()
    }

    private fun credentials() =
        Base64.getEncoder().encodeToString("${serviceuserConfig.username}:${serviceuserConfig.password}".toByteArray(Charsets.UTF_8))
}
