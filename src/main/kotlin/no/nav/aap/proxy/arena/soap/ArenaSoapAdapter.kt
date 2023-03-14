package no.nav.aap.proxy.arena.soap

import org.springframework.stereotype.Component

@Component
class ArenaSoapAdapter( private val sak: ArenaSakSoapAdapter, val oppgave: ArenaOppgaveSoapAdapter)  {

    fun nyesteAktiveSak(fnr: String) = sak.nyesteAktiveSak(fnr)

    fun opprettOppgave(params: ArenaOpprettOppgaveParams)  = oppgave.opprettOppgave(params)

}