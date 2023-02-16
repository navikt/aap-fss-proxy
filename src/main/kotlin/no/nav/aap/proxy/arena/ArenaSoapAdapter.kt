package no.nav.aap.proxy.arena

import jakarta.xml.bind.JAXBElement
import javax.xml.datatype.XMLGregorianCalendar
import no.nav.aap.proxy.arena.generated.sak.Bruker
import no.nav.aap.proxy.arena.generated.sak.HentSaksInfoListeRequestV2
import no.nav.aap.proxy.arena.generated.sak.HentSaksInfoListeV2Response
import no.nav.aap.proxy.arena.generated.sak.ObjectFactory
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.StringExtensions.partialMask
import org.springframework.stereotype.Component
import org.springframework.ws.client.core.WebServiceOperations

@Component
class ArenaSoapAdapter(private val operations: WebServiceOperations, private val cfg: ArenaSoapConfig) {

    private val log = getLogger(javaClass)

    fun hentSaker(fnr: String) =
        if (cfg.enabled) {
            (operations.marshalSendAndReceive(cfg.sakerURI,sakerReq(fnr)) as JAXBElement<HentSaksInfoListeV2Response>).value
                .saksInfoListe.saksInfo
                .filter { it.sakstatus.equals(AKTIV,ignoreCase = true) }
                .filterNot { it.sakstypekode.equals(KLAGEANKE, ignoreCase = true) }
                .sortedByDescending { it.sakOpprettet.toLocalDateTime() }.also {
                    log.info("Saker for ${fnr.partialMask()} er $it")
                }
        } else {
            emptyList()
        }

    private fun sakerReq(fnr: String)  =
        ObjectFactory().createHentSaksInfoListeV2(HentSaksInfoListeRequestV2().apply {
            bruker = Bruker().apply {
                brukerId = fnr
                brukertypeKode = PERSON
            }
            tema = AAP.uppercase()
            isLukket = false
        })

    fun opprettOppgave(fnr: String) = Unit

   private fun XMLGregorianCalendar.toLocalDateTime() = toGregorianCalendar().toZonedDateTime().toLocalDateTime()

    companion object {
        private const val PERSON = "PERSON"
        private const val AKTIV = "Aktiv"
        private const val KLAGEANKE = "KLAN"
    }
}