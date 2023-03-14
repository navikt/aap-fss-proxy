package no.nav.aap.proxy.arena.soap

import no.nav.aap.health.Pingable
import org.springframework.stereotype.Component

@Component
class ArenaSoapAdapter( private val sak: ArenaSakSoapAdapter, val oppgave: ArenaOppgaveSoapAdapter)  {

    fun nyesteAktiveSak(fnr: String) = sak.nyesteAktiveSak(fnr)

    fun opprettOppgave(params: ArenaOpprettOppgaveParams)  = oppgave.opprettOppgave(params)

}

abstract class AbstractPingableSoapAdapter(protected val cfg: ArenaSoapConfig) : Pingable {
    override fun isEnabled() = cfg.enabled
    override fun name() = javaClass.simpleName
}