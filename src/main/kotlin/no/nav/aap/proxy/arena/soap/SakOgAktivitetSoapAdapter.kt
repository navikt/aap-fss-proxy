package no.nav.aap.proxy.arena.soap

import no.nav.aap.proxy.arena.generated.behandleSakOgAktivitet.BehandleKjoerelisteOgOpprettOppgaveRequest
import no.nav.aap.proxy.arena.generated.behandleSakOgAktivitet.BehandleSakOgAktivitetV1
import no.nav.aap.proxy.arena.soap.ArenaSakId.Companion.EMPTY
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Component

@Component
class SakOgAktivitetSoapAdapter(private val behandleSakOgAktivitet: BehandleSakOgAktivitetV1, cfg : ArenaSoapConfig) : ArenaAbstractPingableSoapAdapter(cfg) {

    private val log = getLogger(javaClass)

    fun behandleKjoerelisteOgOpprettOppgave(journalpostId: String) =
        if (cfg.enabled) {
            runCatching {
                behandleSakOgAktivitet.behandleKjoerelisteOgOpprettOppgave(
                    BehandleKjoerelisteOgOpprettOppgaveRequest().apply { this.journalpostId = journalpostId }
                ).let { ArenaSakId(it.saksnummer) }
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
        behandleSakOgAktivitet.ping()
        log.info("ping til behandleSakOgAktivtet vellykket")
        return mapOf("ping" to "OK")
    }
}
