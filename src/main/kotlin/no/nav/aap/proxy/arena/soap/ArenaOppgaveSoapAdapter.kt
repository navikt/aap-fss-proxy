package no.nav.aap.proxy.arena.soap

import no.nav.aap.proxy.arena.generated.oppgave.BehandleArbeidOgAktivitetOppgaveV1
import no.nav.aap.proxy.arena.soap.ArenaDTOs.oppgaveReq
import no.nav.aap.proxy.arena.soap.OpprettetOppgave.Companion.EMPTY
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Component

@Component
class ArenaOppgaveSoapAdapter(val arenaOppgave: BehandleArbeidOgAktivitetOppgaveV1, cfg: ArenaSoapConfig) : AbstractPingableSoapAdapter(cfg) {

    private val log = getLogger(javaClass)

    fun opprettOppgave(params: ArenaOpprettOppgaveParams)  =
        if (cfg.enabled) {
            runCatching {
                arenaOppgave.bestillOppgave(oppgaveReq(params)).let {
                    OpprettetOppgave(it.oppgaveId,it.arenaSakId)
                }
            }.getOrElse {
                log.warn("Opprettelse av oppgave feilet",it)
                throw it
            }
        }
        else {
            EMPTY
        }

    override fun pingEndpoint() = cfg.oppgaveUri

    override fun ping(): Map<String, String> {
        arenaOppgave.ping()
        return mapOf("ping" to "OK")
    }
}