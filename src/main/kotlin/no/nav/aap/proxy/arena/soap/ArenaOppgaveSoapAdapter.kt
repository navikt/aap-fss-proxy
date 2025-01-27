package no.nav.aap.proxy.arena.soap

import no.nav.aap.proxy.arena.generated.oppgave.BehandleArbeidOgAktivitetOppgaveV1
import no.nav.aap.proxy.arena.generated.oppgave.WSBestillOppgaveResponse
import org.springframework.stereotype.Component
import no.nav.aap.proxy.arena.soap.ArenaDTOs.oppgaveReq
import no.nav.aap.proxy.arena.soap.OpprettetOppgave.Companion.EMPTY
import no.nav.aap.util.LoggerUtil.getLogger
import java.lang.NullPointerException


// Wrokaround, da å kall getArenaSakId når arenaSakId er null kaster en nullpointer exception til tross for at wsdl sier at den kan være null
data class BesillOppgaveResponseWrapper(private val response: WSBestillOppgaveResponse) {
    val oppgaveId = response.oppgaveId
    val arenaSakId: String?
        get() = try {
            response.arenaSakId
        } catch (e: NullPointerException) {
            null
        }
}

@Component
class ArenaOppgaveSoapAdapter(private val arenaOppgave : BehandleArbeidOgAktivitetOppgaveV1, cfg : ArenaSoapConfig) : ArenaAbstractPingableSoapAdapter(cfg) {

    private val log = getLogger(javaClass)

    fun opprettOppgave(params : ArenaOpprettOppgaveParams) =
        if (cfg.enabled) {
            runCatching {
                arenaOppgave.bestillOppgave(oppgaveReq(params))
                    .let(::BesillOppgaveResponseWrapper).let {
                    OpprettetOppgave(it.oppgaveId, it.arenaSakId)
                }
            }.getOrElse {
                log.warn("Opprettelse av oppgave feilet", it)
                throw it
            }
        }
        else {
            EMPTY
        }

    override fun pingEndpoint() = cfg.oppgaveUri

    override fun ping() : Map<String, String> {
        arenaOppgave.ping()
        return mapOf("ping" to "OK")
    }
}