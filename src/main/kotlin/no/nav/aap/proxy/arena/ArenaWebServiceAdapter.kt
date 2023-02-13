package no.nav.aap.proxy.arena

import java.time.LocalDate
import java.util.*
import javax.naming.ServiceUnavailableException
import javax.xml.datatype.DatatypeFactory
import no.nav.aap.api.felles.Fødselsnummer
import org.jalla.Bruker
import org.jalla.HentSaksInfoListeRequestV2
import org.jalla.HentSaksInfoListeV2Response
import org.jalla.SaksInfoListe
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.ws.client.core.WebServiceOperations

@Component
class ArenaWebServiceAdapter(private val operations: WebServiceOperations) {

    fun hentSaker(brukerId: String, brukertype: String, saksId: Int, fom: LocalDate,
                  tom: LocalDate, tema: String, lukket: Boolean): SaksInfoListe {
        val req = HentSaksInfoListeRequestV2().apply {
            bruker = Bruker().apply {
                setBrukerId(brukerId)
                brukertypeKode = brukertype
            }
            setFomDato(fom.toGreorian())
            setTomDato(tom.toGreorian())
            setSaksId(saksId)
            setTema(tema)
            isLukket = lukket
        }
           val res = operations.marshalSendAndReceive(req) as HentSaksInfoListeV2Response
        return res.saksInfoListe
    }

    fun harAktivSak(fnr: Fødselsnummer)  =  false  // TODO

    private fun LocalDate.toGreorian() = FACTORY.newXMLGregorianCalendar(toString())

    companion object {
        private val FACTORY = DatatypeFactory.newInstance()
        private val log = LoggerFactory.getLogger(ArenaWebServiceAdapter::class.java)
        private const val TIMEOUT = 12000
    }
}