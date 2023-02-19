package no.nav.aap.proxy.arena

import no.nav.aap.api.felles.Fødselsnummer
import org.springframework.stereotype.Component

@Component
class ArenaClient(private val rest: ArenaRestAdapter, val soap: ArenaSoapAdapter) {
    fun sisteVedtak(fnr: Fødselsnummer) = rest.sisteVedtak(fnr.fnr)
    fun harAktivSak(fnr: Fødselsnummer) = soap.harAktivSak(fnr.fnr)
    fun opprettOppgave(params: ArenaOpprettOppgaveParams) = soap.opprettOppgave(params)
}