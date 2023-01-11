package no.nav.aap.proxy.arena

import no.nav.aap.api.felles.Fødselsnummer
import org.springframework.stereotype.Component

@Component
class ArenaClient(private val a: ArenaWebClientAdapter) {
    fun sisteVedtak(fnr: Fødselsnummer) = a.getSisteVedtak(fnr.fnr)
}