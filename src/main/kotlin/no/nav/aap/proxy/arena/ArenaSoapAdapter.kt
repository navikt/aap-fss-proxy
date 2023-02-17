package no.nav.aap.proxy.arena

import jakarta.xml.bind.JAXBElement
import java.net.URL
import no.nav.aap.proxy.arena.ArenaDTOs.oppgaveReq1
import no.nav.aap.proxy.arena.ArenaDTOs.sakerReq
import no.nav.aap.proxy.arena.ArenaDTOs.toLocalDateTime
import no.nav.aap.proxy.arena.TheSTSUtil.wrapWithSts
import no.nav.aap.proxy.arena.generated.oppgave.BehandleArbeidOgAktivitetOppgaveV1_Service
import no.nav.aap.proxy.arena.generated.sak.HentSaksInfoListeV2Response
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.StringExtensions.partialMask
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.ws.client.core.WebServiceOperations

@Component
class ArenaSoapAdapter(@Qualifier("sak") private val sak: WebServiceOperations, @Qualifier("oppgave") private val oppgave: WebServiceOperations , @Qualifier("sts") private val sts: WebServiceOperations, private val cfg: ArenaSoapConfig) {

    private val log = getLogger(javaClass)

    fun hentSaker(fnr: String) =
        if (cfg.enabled) {
            (sak.marshalSendAndReceive(cfg.sakerURI,sakerReq(fnr)) as JAXBElement<HentSaksInfoListeV2Response>).value
                .saksInfoListe.saksInfo
                .filter { it.sakstatus.equals(AKTIV,ignoreCase = true) }
                .filterNot { it.sakstypekode.equals(KLAGEANKE, ignoreCase = true) }
                .sortedByDescending { it.sakOpprettet.toLocalDateTime() }.also {
                    log.info("Saker for ${fnr.partialMask()} er $it")
                }
        } else {
            emptyList()
        }
    fun opprettOppgave(params: ArenaOpprettOppgaveParams) =
        if (cfg.enabled) {
            val p = wrapWithSts(BehandleArbeidOgAktivitetOppgaveV1_Service(URL("https://arena-q1.adeo.no/ail_ws/BehandleArbeidOgAktivitetOppgave_v1?wsdl")).behandleArbeidOgAktivitetOppgaveV1Port, "user","pw"," https://sts-q1.preprod.local/SecurityTokenServiceProvider")
            var rs = p.bestillOppgave(oppgaveReq1(params))
            log.info("RS er $rs")
          //  (oppgave.marshalSendAndReceive(cfg.oppgaveUri,oppgaveReq(params)) as JAXBElement<BestillOppgaveResponse>).value.also {
          //      log.info("Opprettet oppgave $it")
          //  }
        }
        else Unit

    companion object {
        private const val AKTIV = "Aktiv"
        private const val KLAGEANKE = "KLAN"
    }
}