package no.nav.aap.proxy.arena

import jakarta.xml.bind.JAXBElement
import no.nav.aap.proxy.arena.ArenaDTOs.oppgaveReq
import no.nav.aap.proxy.arena.ArenaDTOs.saker
import no.nav.aap.proxy.arena.ArenaDTOs.toLocalDateTime
import no.nav.aap.proxy.arena.generated.oppgave.BestillOppgaveResponse
import no.nav.aap.proxy.arena.generated.sak.HentSaksInfoListeV2Response
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.StringExtensions.partialMask
import org.springframework.stereotype.Component
import org.springframework.ws.client.core.WebServiceOperations

@Component
class ArenaSoapAdapter(private val operations: WebServiceOperations, private val cfg: ArenaSoapConfig) {

    private val log = getLogger(javaClass)

    fun hentSaker(fnr: String) =
        if (cfg.enabled) {
            (operations.marshalSendAndReceive(cfg.sakerURI,saker(fnr)) as JAXBElement<HentSaksInfoListeV2Response>).value
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
            (operations.marshalSendAndReceive(cfg.oppgaveUri,oppgaveReq(params)) as JAXBElement<BestillOppgaveResponse>).value.also {
                log.info("Opprettet oppgave $it")
            }
        }
        else Unit

    companion object {
        private const val AKTIV = "Aktiv"
        private const val KLAGEANKE = "KLAN"
    }
}