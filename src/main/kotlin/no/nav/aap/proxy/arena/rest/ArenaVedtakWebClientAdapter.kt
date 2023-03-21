package no.nav.aap.proxy.arena.rest

import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.proxy.arena.rest.ArenaVedtakRestConfig.Companion.ARENA
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.WebClientExtensions.toResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class ArenaVedtakWebClientAdapter(@Qualifier(ARENA) webClient: WebClient, private val cf: ArenaVedtakRestConfig) : AbstractWebClientAdapter(webClient, cf) {

    fun sisteVedtak(fnr: String) =
        webClient
            .get()
            .uri { b -> b.path(cf.path).build() }
            .header("fnr", fnr)
            .exchangeToMono { it.toResponse<ByteArray>(log)}
            .doOnError { t: Throwable -> log.warn("Arenaoppslag feilet", t) }
            .doOnSuccess { log.trace("Arena oppslag OK") }
            .retryWhen(cf.retrySpec(log))
            .block() ?: throw IrrecoverableIntegrationException("Null respons fra arena vedtak")
            .also { log.trace("Arena response $it") }

}