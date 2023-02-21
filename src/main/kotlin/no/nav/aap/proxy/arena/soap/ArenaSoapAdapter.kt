package no.nav.aap.proxy.arena.soap

import jakarta.xml.bind.JAXBElement
import jakarta.xml.ws.BindingProvider.*
import java.util.*
import no.nav.aap.proxy.arena.generated.oppgave.BehandleArbeidOgAktivitetOppgaveV1
import no.nav.aap.proxy.arena.generated.sak.HentSaksInfoListeV2Response
import no.nav.aap.proxy.arena.soap.ArenaDTOs.oppgaveReq
import no.nav.aap.proxy.arena.soap.ArenaDTOs.sakerReq
import no.nav.aap.proxy.arena.soap.ArenaDTOs.toLocalDateTime
import no.nav.aap.proxy.arena.soap.ArenaSoapConfig.Companion.SAK
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.StringExtensions.partialMask
import org.apache.cxf.rt.security.SecurityConstants.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.ws.client.core.WebServiceOperations

@Component
class ArenaSoapAdapter(@Qualifier(SAK) private val sak: WebServiceOperations, val oppgave: BehandleArbeidOgAktivitetOppgaveV1, private val cfg: ArenaSoapConfig) {

    private val log = getLogger(javaClass)

    fun nyesteAktiveSak(fnr: String) =
        (sak.marshalSendAndReceive(cfg.sakerURI, sakerReq(fnr)) as JAXBElement<HentSaksInfoListeV2Response>).value
            .saksInfoListe.saksInfo
            .filter { it.sakstatus.equals(AKTIV, ignoreCase = true) }
            .filterNot { it.sakstypekode.equals(KLAGEANKE, ignoreCase = true) }
            .sortedByDescending { it.sakOpprettet.toLocalDateTime() }.also {
                log.info("Saker for ${fnr.partialMask()} er ${it.map { s -> s.saksId }}")
            }.firstOrNull()?.let {
                it.saksId
            }

    fun opprettOppgave(params: ArenaOpprettOppgaveParams)  =
        runCatching {
            oppgave.bestillOppgave(oppgaveReq(params)).let {
                OpprettetOppgave(it.oppgaveId,it.arenaSakId)
            }
        }.getOrElse {
            log.warn("Opprettelse av oppgave feilet")
            throw it
        }

    companion object {
        private const val AKTIV = "Aktiv"
        private const val KLAGEANKE = "KLAN"
    }
}