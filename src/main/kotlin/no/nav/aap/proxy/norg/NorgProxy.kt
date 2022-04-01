package no.nav.aap.proxy.norg

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.aap.util.Constants.NORG
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.net.URI

@RestController("/norg")
class NorgController(private val norg: NorgClient) {

    @PostMapping("/arbeidsfordeling")
    fun hentArbeidsfordeling(@RequestBody request: ArbeidsfordelingRequest) =
        norg.hentArbeidsfordeling(request)
}

@Component
class NorgClient(
    @Qualifier(NORG) client: WebClient,
    config: NorgConfig,
) : AbstractWebClientAdapter(client, config) {
    fun hentArbeidsfordeling(request: ArbeidsfordelingRequest) =
        webClient
            .post()
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono<ArbeidsfordelingResponse>()
            .block()
}

@Component
class NorgHealthIndicator(client: NorgClient) : AbstractPingableHealthIndicator(client)

@Configuration
class NorgBeanConfig(@Value("\${spring.application.name}") val appName: String, val config: NorgConfig) {
    @Bean
    @Qualifier(NORG)
    fun client(builder: WebClient.Builder, stsExchange: ExchangeFilterFunction): WebClient =
        builder.baseUrl("${config.baseUri}").filter(correlatingFilterFunction(appName)).filter(stsExchange).build()
}

@ConstructorBinding
@ConfigurationProperties("norg")
class NorgConfig(
    baseUri: URI,
    @DefaultValue("/api/v1/arbeidsfordeling/enheter/bestmatch") val path: String,
    @DefaultValue("internal/isAlive") pingPath: String,
    @DefaultValue("true") enabled: Boolean
) : AbstractRestConfig(baseUri, pingPath, enabled)

data class ArbeidsfordelingRequest(
    val geografiskOmraade: String,
    val tema: String,
    val behandlingstema: String,
    val skjermet: Boolean,
    val diskresjonskode: String,
)

data class ArbeidsfordelingResponse(val enhetNr: String)