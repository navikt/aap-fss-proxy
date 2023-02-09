package no.nav.aap.proxy.arena

import no.nav.aap.api.felles.Fødselsnummer

@Component
class ArenaClient(private val a: ArenaWebClientRestAdapter) {
    fun sisteVedtak(fnr: Fødselsnummer) = a.sisteVedtak(fnr.fnr)
}