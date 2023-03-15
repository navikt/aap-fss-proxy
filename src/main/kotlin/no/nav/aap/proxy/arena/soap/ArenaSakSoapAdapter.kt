package no.nav.aap.proxy.arena.soap

import jakarta.xml.bind.JAXBElement
import jakarta.xml.ws.BindingProvider.*
import java.util.*
import no.nav.aap.proxy.arena.generated.sak.HentSaksInfoListeV2Response
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
class ArenaSakSoapAdapter(@Qualifier(SAK) private val sak: WebServiceOperations, cfg: ArenaSoapConfig) : ArenaAbstractPingableSoapAdapter(cfg) {

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

    override fun ping(): Map<String, String> {
        nyesteAktiveSak("11111111111")
        return mapOf("ping" to "OK") 
    }
    override fun pingEndpoint() = cfg.sakerURI

    companion object {
        private const val AKTIV = "Aktiv"
        private const val KLAGEANKE = "KLAN"
    }
}