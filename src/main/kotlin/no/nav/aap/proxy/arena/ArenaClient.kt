package no.nav.aap.proxy.arena

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.proxy.arena.rest.ArenaRestAdapter
import no.nav.aap.proxy.arena.soap.ArenaOpprettOppgaveParams
import no.nav.aap.proxy.arena.soap.ArenaSoapAdapter
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity.*
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ResponseStatus

@Component
class ArenaClient(private val rest: ArenaRestAdapter, val soap: ArenaSoapAdapter) {
    fun sisteVedtak(fnr: Fødselsnummer) = rest.sisteVedtak(fnr.fnr)
    fun nyesteSak(fnr: Fødselsnummer) = soap.nyesteAktiveSak(fnr.fnr)
    @ResponseStatus(CREATED)
    fun opprettOppgave(params: ArenaOpprettOppgaveParams) = soap.opprettOppgave(params)
}