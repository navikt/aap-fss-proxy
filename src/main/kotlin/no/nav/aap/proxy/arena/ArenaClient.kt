package no.nav.aap.proxy.arena

import io.micrometer.observation.annotation.Observed
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.proxy.arena.soap.ArenaOppgaveSoapAdapter
import no.nav.aap.proxy.arena.soap.ArenaOpprettOppgaveParams
import no.nav.aap.proxy.arena.soap.ArenaSakSoapAdapter
import no.nav.aap.proxy.arena.soap.SakOgAktivitetSoapAdapter
import org.springframework.http.HttpStatus.CREATED
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ResponseStatus

@Component
@Observed
class ArenaClient(
    private val sak : ArenaSakSoapAdapter,
    private val oppgave : ArenaOppgaveSoapAdapter,
    private val sakOgAktivitet: SakOgAktivitetSoapAdapter
) {

    fun nyesteSak(fnr : Fødselsnummer) = sak.nyesteAktiveSak(fnr.fnr)

    @ResponseStatus(CREATED)
    fun opprettOppgave(params : ArenaOpprettOppgaveParams) = oppgave.opprettOppgave(params)

    fun behandleKjoerelisteOgOpprettOppgave(journalpostId: String) =
        sakOgAktivitet.behandleKjoerelisteOgOpprettOppgave(journalpostId)

}
