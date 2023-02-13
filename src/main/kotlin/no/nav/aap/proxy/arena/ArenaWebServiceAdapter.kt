package no.nav.aap.proxy.arena

import javax.xml.datatype.XMLGregorianCalendar
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.proxy.arena.generated.Bruker
import no.nav.aap.proxy.arena.generated.HentSaksInfoListeRequestV2
import no.nav.aap.proxy.arena.generated.HentSaksInfoListeV2Response
import no.nav.aap.proxy.arena.generated.ObjectFactory
import no.nav.aap.util.Constants.AAP
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.ws.client.core.WebServiceOperations

@Component
class ArenaWebServiceAdapter(private val operations: WebServiceOperations) {

    fun hentSaker(fnr: Fødselsnummer) =
         (operations.marshalSendAndReceive(request(fnr)) as HentSaksInfoListeV2Response).saksInfoListe.saksInfo
             .filter { it.tema.equals(AAP, ignoreCase = true) }
             .filter { it.sakstatus.equals("Aktiv",ignoreCase = true) }
             .filterNot { it.sakstypekode.equals("KLAN", ignoreCase = true) }
             .sortedByDescending { it.sakOpprettet.toLocalDateTime() }

    private fun request(fnr: Fødselsnummer)  =
        ObjectFactory().createHentSaksInfoListeRequestV2()
            .apply {
            bruker = Bruker().apply {
                brukerId = fnr.fnr
                brukertypeKode = PERSON
            }
            tema = AAP
            isLukket = false
        }


    private fun XMLGregorianCalendar.toLocalDateTime() = toGregorianCalendar().toZonedDateTime().toLocalDateTime()


    companion object {
        const val PERSON = "PERSON"
        private val log = LoggerFactory.getLogger(ArenaWebServiceAdapter::class.java)
    }
}