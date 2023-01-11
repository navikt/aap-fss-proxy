package no.nav.aap.proxy.arena

import io.micrometer.observation.annotation.Observed
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.proxy.sts.StsWebClientAdapter
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.net.URI
import java.util.*

@ConfigurationProperties("arena")
class ArenaConfig (baseUri: URI,
                   @DefaultValue("aap/sisteVedtak") val path: String,
                       @DefaultValue("true") enabled: Boolean): AbstractRestConfig(baseUri,"",
    "arena",enabled)

@Configuration
class ArenaClientConfig(private val cfg: ArenaConfig) {

    @Bean
    @Qualifier("arena")
    fun arenaWebClient(builder: WebClient.Builder, @Qualifier("arenaoidc") arenaExchangeFilterFunction: ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(arenaExchangeFilterFunction)
            .build()

    @Bean
    fun arenaHealthIndicator(a: StsWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}

}

@Component
class ArenaWebClientAdapter(@Qualifier("arena") webClient: WebClient, private val cf: ArenaConfig) : AbstractWebClientAdapter(webClient, cf) {

    fun getSisteVedtak(fnr: String) =
        webClient
            .get()
            .uri { b -> b.path(cf.path).build() }
            .header("fnr", fnr)
            .retrieve()
            .bodyToMono<ArenaResponse>()
            .doOnError { t: Throwable -> log.warn("Arenaoppslag feilet", t) }
            .doOnSuccess { log.trace("Arenaoppslag OK") }
            .block()
            .also { log.trace("Arena response $it") }
}

@Component
@Observed
class ArenaClient(private val a: ArenaWebClientAdapter) {
    fun getSisteVedtak(fnr: String) = a.getSisteVedtak(fnr)
}
