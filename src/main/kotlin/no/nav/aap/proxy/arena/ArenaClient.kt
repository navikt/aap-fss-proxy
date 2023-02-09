package no.nav.aap.proxy.arena

import io.micrometer.observation.annotation.Observed
import no.nav.aap.api.felles.Fødselsnummer
import org.springframework.stereotype.Component
<<<<<<< HEAD
=======
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.net.URI
import java.util.*

@ConfigurationProperties("arena")
class ArenaConfig(baseUri: URI,
                  @DefaultValue("/aap/sisteVedtak") val path: String,
                  @DefaultValue("true") enabled: Boolean, retry: RetryConfig = RetryConfig.DEFAULT): AbstractRestConfig(baseUri,"",
    "arena",enabled, retry)

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
            .bodyToMono<ByteArray>()
            .doOnError { t: Throwable -> log.warn("Arenaoppslag feilet", t) }
            .doOnSuccess { log.trace("Arenaoppslag OK") }
            .block()
            .also { log.trace("Arena response $it") }
}

>>>>>>> 4c5fbbb (janoalav gjør ting han ikke kan)
@Component
@Observed
class ArenaClient(private val a: ArenaWebClientRestAdapter) {
    fun sisteVedtak(fnr: Fødselsnummer) = a.sisteVedtak(fnr.fnr)