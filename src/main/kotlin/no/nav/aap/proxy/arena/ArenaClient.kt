package no.nav.aap.proxy.arena

import io.micrometer.observation.annotation.Observed
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity.*
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ResponseStatus
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.proxy.arena.rest.ArenaVedtakWebClientAdapter
import no.nav.aap.proxy.arena.soap.ArenaOppgaveSoapAdapter
import no.nav.aap.proxy.arena.soap.ArenaOpprettOppgaveParams
import no.nav.aap.proxy.arena.soap.ArenaSakSoapAdapter

@Component
@Observed
class ArenaClient(private val vedtak : ArenaVedtakWebClientAdapter, val sak : ArenaSakSoapAdapter, val oppgave : ArenaOppgaveSoapAdapter) {

    fun sisteVedtak(fnr : Fødselsnummer) = vedtak.sisteVedtak(fnr.fnr)
    fun nyesteSak(fnr : Fødselsnummer) = sak.nyesteAktiveSak(fnr.fnr)

    @ResponseStatus(CREATED)
    fun opprettOppgave(params : ArenaOpprettOppgaveParams) = oppgave.opprettOppgave(params)

}
