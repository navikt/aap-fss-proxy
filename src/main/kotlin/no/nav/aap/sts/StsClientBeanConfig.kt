package no.nav.aap.sts

import no.nav.aap.config.Constants.STS
import no.nav.aap.config.ServiceuserConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.ClientRequest
import java.util.Base64.getEncoder
@Configuration
class StsClientBeanConfig(val cfg: ServiceuserConfig) {

    @Bean
    @Qualifier(STS)
    fun stsWebClient(builder: WebClient.Builder, stsCfg: StsConfig): WebClient {
        return builder
            .baseUrl("${stsCfg.baseUri}/rest/v1/sts/token")
            .filter(stsExchangeFilterFunction())
            .build()
    }

    private fun stsExchangeFilterFunction() =
        ExchangeFilterFunction { req, next -> next.exchange(ClientRequest.from(req).header(AUTHORIZATION, "Basic ${credentials()}").build()) }

    private fun credentials() =
        getEncoder().encodeToString("${cfg.username}:${cfg.password}".toByteArray(Charsets.UTF_8))
}