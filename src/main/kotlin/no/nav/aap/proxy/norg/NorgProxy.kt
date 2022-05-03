package no.nav.aap.proxy.norg

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.Constants.NORG
import org.springframework.beans.factory.annotation.Qualifier
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
class NorgController(private val norg: NorgWebClientAdapter) {

    @PostMapping("/arbeidsfordeling")
    fun hentArbeidsfordeling(@RequestBody request: ArbeidRequest) = norg.hentArbeidsfordeling(request)
}

@Component
class NorgWebClientAdapter(
    @Qualifier(NORG) client: WebClient, config: NorgConfig) : AbstractWebClientAdapter(client, config) {
    fun hentArbeidsfordeling(request: ArbeidRequest) =
        webClient
            .post()
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono<ArbeidsResponse>()
            .doOnError { t: Throwable -> log.warn("Norg feilet", t) }
            .doOnSuccess { log.trace("Norg OK") }
            .block()
            .also { log.trace("Norg response $it") }
}

@Configuration
class NorgBeanConfig(val config: NorgConfig) {
    @Bean
    fun norgHealthIndicator(a: NorgWebClientAdapter) =  object : AbstractPingableHealthIndicator(a){}

    @Bean
    @Qualifier(NORG)
    fun client(builder: WebClient.Builder, stsExchange: ExchangeFilterFunction): WebClient =
        builder
            .baseUrl("${config.baseUri}")
            .filter(stsExchange).build()
}

@ConstructorBinding
@ConfigurationProperties(NORG)
class NorgConfig(
        baseUri: URI,
        @DefaultValue("/api/v1/arbeidsfordeling/enheter/bestmatch") val path: String,
        @DefaultValue("internal/isAlive") pingPath: String,
        @DefaultValue("true") enabled: Boolean) : AbstractRestConfig(baseUri, pingPath, enabled)

data class ArbeidRequest(
    val geografiskOmraade: String,
    val tema: String,
    val behandlingstema: String,
    val skjermet: Boolean,
    val diskresjonskode: String)

data class ArbeidsResponse(val enhetNr: String)