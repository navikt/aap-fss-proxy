package no.nav.aap.proxy.organisasjon

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.proxy.organisasjon.OrganisasjonConfig.Companion.ORGANISASJON
import no.nav.aap.proxy.sts.StsClient
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.beans.factory.annotation.Value

@Configuration
class OrganisasjonClientBeanConfig(@Value("\${spring.application.name}") val applicationName: String, val cfg: OrganisasjonConfig) {

    @Bean
    @Qualifier(ORGANISASJON)
    fun organisasjonkWebClient(stsClient: StsClient,builder: WebClient.Builder) =
        builder
            .baseUrl(cfg.baseUri.toString())
            .filter(correlatingFilterFunction(applicationName))
            .build()

    //@Bean Ser ikke ut til å ha noe ping-endepunkt.
    fun organisasjonHealthIndicator(a: OrganisasjonWebClientAdapter) = object : AbstractPingableHealthIndicator(a){
    }
}