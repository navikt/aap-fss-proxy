package no.nav.aap.sts

import no.nav.aap.config.ServiceuserConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.*
import java.util.Base64.*

@Configuration
class StsClientConfig(private val cfg: ServiceuserConfig) {

    @Bean
    fun stsWebClient(builder: WebClient.Builder, stsConfig: StsConfig): WebClient {
        return builder
            .baseUrl("${stsConfig.url}/rest/v1/sts/token")
            .filter(stsExchangeFilterFunction(cfg))
            .build()
    }

    private fun stsExchangeFilterFunction(cfg: ServiceuserConfig) =
        ExchangeFilterFunction { req, next -> next.exchange(ClientRequest.from(req).header(HttpHeaders.AUTHORIZATION, "Basic ${credentials()}").build()) }

    private fun credentials() =
        getEncoder().encodeToString("${cfg.username}:${cfg.password}".toByteArray(Charsets.UTF_8))
}