package no.nav.aap.proxy.joark

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.joark.JoarkResponse
import no.nav.aap.joark.Journalpost
import no.nav.aap.proxy.joark.JoarkClientBeanConfig.JoarkClient
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.aap.util.Constants
import no.nav.aap.util.Constants.AAD
import no.nav.aap.util.Constants.JOARK
import no.nav.boot.conditionals.EnvUtil.isDevOrLocal
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.netty.http.client.HttpClient
import java.net.URI


@ProtectedRestController(value = ["/joark"], issuer = AAD, claimMap =[""])
class JoarkController(private val joark: JoarkClient) {
    @PostMapping("/aad")
    fun opprettJournalpostFraSaksbehandler(@RequestBody journalpost: Journalpost) = joark.opprettJournalpost(journalpost)
}

@Configuration
class JoarkClientBeanConfig(@Value("\${spring.application.name}") private val applicationName: String, private val cfg: JoarkConfig) {

    @Bean
    @Qualifier(Constants.JOARK)
    fun joarkWebClient(builder: WebClient.Builder, stsExchangeFilterFunction: ExchangeFilterFunction, env: Environment) =
        builder.baseUrl("${cfg.baseUri}")
            .clientConnector(ReactorClientHttpConnector(HttpClient.create().wiretap(isDevOrLocal(env))))
            .filter(correlatingFilterFunction(applicationName))
            .filter(stsExchangeFilterFunction)
            .build()

    @Component
    class JoarkClient(val a: JoarkWebClientAdapter) {
        fun opprettJournalpost(journalpost: Journalpost)  = a.opprettJournalpost(journalpost)
    }
    @Component
    class JoarkWebClientAdapter(@Qualifier(JOARK) webClient: WebClient, private val cf: JoarkConfig) : AbstractWebClientAdapter(webClient, cf) {
        fun opprettJournalpost(journalpost: Journalpost) =
            webClient
                .post()
                .uri { b -> b.path(cf.path).build() }
                .contentType(APPLICATION_JSON)
                .bodyValue(journalpost)
                .retrieve()
                .bodyToMono<JoarkResponse>()
                .doOnError { t: Throwable -> log.warn("Joark arkivering av $journalpost feilet", t) }
                .block()
                .also {  log.trace("Joark response er $it")}
    }

    @Bean
    fun joarkHealthIndicator(a: JoarkWebClientAdapter) = object : AbstractPingableHealthIndicator(a){}
}

@ConfigurationProperties("joark")
@ConstructorBinding
class JoarkConfig(baseUri: URI,
                  @DefaultValue("rest/journalpostapi/v1/journalpost") val path: String,
                  @DefaultValue("isAlive") pingPath: String,
                  @DefaultValue("true") enabled: Boolean): AbstractRestConfig(baseUri,pingPath,enabled)