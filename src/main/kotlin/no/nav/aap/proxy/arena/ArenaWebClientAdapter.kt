package no.nav.aap.proxy.arena

import no.nav.aap.proxy.arena.ArenaConfig.Companion.ARENA
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class ArenaWebClientAdapter(@Qualifier(ARENA) webClient: WebClient, private val cf: ArenaConfig) : AbstractWebClientAdapter(webClient, cf) {

    fun getSisteVedtak(fnr: String) =
        webClient
            .get()
            .uri { b -> b.path(cf.path).build() }
            .header("fnr", fnr)
            .retrieve()
            .bodyToMono<ArenaResponse>()
            .retryWhen(cf)
            .doOnError { t: Throwable -> log.warn("Arenaoppslag feilet", t) }
            .doOnSuccess { log.trace("Arenaoppslag OK") }
            .block()
            .also { log.trace("Arena response $it") }
}