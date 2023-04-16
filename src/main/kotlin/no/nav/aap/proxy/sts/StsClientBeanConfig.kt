package no.nav.aap.proxy.sts

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.proxy.config.ServiceuserConfig
import no.nav.aap.util.Constants.STS

@Configuration(proxyBeanMethods = false)
class StsClientBeanConfig(private val cfg : ServiceuserConfig) {

    @Bean
    @Qualifier(STS)
    fun stsWebClient(builder : WebClient.Builder, stsCfg : StsConfig) =
        builder
            .baseUrl("${stsCfg.baseUri}")
            .filter(stsExchangeFilterFunction())
            .build()

    @Bean
    fun stsHealthIndicator(a : StsWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}

    private fun stsExchangeFilterFunction() =
        ExchangeFilterFunction { req, next ->
            next.exchange(ClientRequest.from(req).header(AUTHORIZATION, "Basic ${cfg.credentials}")
                .build())
        }
}