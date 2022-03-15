package no.nav.aap.proxy.organisasjon

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.proxy.sts.StsClient
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.StringExtensions.asBearer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

@Configuration
class OrganisasjonClientBeanConfig(@Value("\${spring.application.name}") val applicationName: String, val cfg: OrganisasjonConfig) {

    @Bean
    @Qualifier(OrganisasjonConfig.ORGANISASJON)
    fun organisasjonkWebClient(stsClient: StsClient,builder: WebClient.Builder) =
        builder
            .baseUrl(cfg.baseUri.toString())
            .filter(correlatingFilterFunction(applicationName))
            .build()

    @Bean
    fun organisasjonHealthIndicator(a: OrganisasjonWebClientAdapter) = object : AbstractPingableHealthIndicator(a){
    }
}