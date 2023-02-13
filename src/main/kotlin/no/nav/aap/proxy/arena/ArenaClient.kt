package no.nav.aap.proxy.arena

import no.nav.aap.api.felles.Fødselsnummer
import org.springframework.stereotype.Component

@Component
class ArenaClient(private val a: ArenaWebClientRestAdapter, val soap: ArenaWebServiceAdapter) {
    fun sisteVedtak(fnr: Fødselsnummer) = a.sisteVedtak(fnr.fnr)

    fun harAktivSak(fnr: Fødselsnummer) = false // TODO
}