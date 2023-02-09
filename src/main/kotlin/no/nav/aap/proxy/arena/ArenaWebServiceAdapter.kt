package no.nav.aap.proxy.arena

import java.util.*
import javax.naming.ServiceUnavailableException
import javax.xml.datatype.XMLGregorianCalendar
import org.jalla.Bruker
import org.jalla.HentSaksInfoListeRequestV2
import org.jalla.HentSaksInfoListeV2Response
import org.jalla.SaksInfoListe
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.ws.client.core.WebServiceTemplate

@Component
class ArenaWebServiceAdapter(private val template: WebServiceTemplate) {

    @Throws(ServiceUnavailableException::class)
    fun fetchSaker(brukerId: String, brukertype: String, saksId: Int, fomDato: XMLGregorianCalendar,
                   tomDato: XMLGregorianCalendar, tema: String, lukket: Boolean): SaksInfoListe {
        val req = HentSaksInfoListeRequestV2().apply {
            bruker = Bruker().apply {
                setBrukerId(brukerId)
                brukertypeKode = brukertype
            }
            setFomDato(fomDato)
            setTomDato(tomDato)
            setSaksId(saksId)
            setTema(tema)
            isLukket = lukket
        }
           val res = template.marshalSendAndReceive(req) as HentSaksInfoListeV2Response
        return res.saksInfoListe
    }

    companion object {
        private val log = LoggerFactory.getLogger(ArenaWebServiceAdapter::class.java)
        private const val TIMEOUT = 12000
    }
}