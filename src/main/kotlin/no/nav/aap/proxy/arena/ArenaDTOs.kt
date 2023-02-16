package no.nav.aap.proxy.arena

import jakarta.xml.bind.JAXBElement
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar
import javax.xml.namespace.QName
import no.nav.aap.proxy.arena.ArenaDTOs.ArenaOppgaveType.STARTVEDTAK
import no.nav.aap.proxy.arena.generated.oppgave.BehandleArbeidOgAktivitetOppgaveV1
import no.nav.aap.proxy.arena.generated.oppgave.WSBestillOppgaveRequest
import no.nav.aap.proxy.arena.generated.oppgave.WSOppgave
import no.nav.aap.proxy.arena.generated.oppgave.WSOppgavetype
import no.nav.aap.proxy.arena.generated.oppgave.WSPerson
import no.nav.aap.proxy.arena.generated.oppgave.WSPrioritet
import no.nav.aap.proxy.arena.generated.oppgave.WSTema
import no.nav.aap.proxy.arena.generated.sak.Bruker
import no.nav.aap.proxy.arena.generated.sak.HentSaksInfoListeRequestV2
import no.nav.aap.proxy.arena.generated.sak.ObjectFactory
import no.nav.aap.proxy.arena.generated.oppgave.ObjectFactory as OppgaveObjectFactory
import no.nav.aap.util.Constants
import no.nav.aap.util.Constants.AAP

object ArenaDTOs {
    fun oppgaveReq(params: ArenaOpprettOppgaveParams): JAXBElement<WSBestillOppgaveRequest> {
    val  r = WSBestillOppgaveRequest().apply {
        oppgavetype = WSOppgavetype().apply {
            value = STARTVEDTAK.name
        }
        OppgaveObjectFactory()
        oppgave = WSOppgave().apply {
            tema = WSTema().apply {
                value = AAP.uppercase()
            }
            bruker = WSPerson().apply {
                ident = params.fnr.fnr
            }
            tilleggsinformasjon = oppgaveBeskrivelse(params.tittel, params.titler)
            behandlendeEnhetId = params.enhet
            prioritet = WSPrioritet().apply {
                value = "HOY"
            }
            beskrivelse =  STARTVEDTAK.tekst
            frist = DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar.from(ZonedDateTime.now().toInstant().atZone(
                    ZoneId.of("Europe/Oslo"))))
        }
    }
        val q =QName("http://nav.no/tjeneste/virksomhet/behandleArbeidOgAktivitetOppgave/v1/Binding", "BehandleArbeidOgAktivitetOppgave_v1")
        return JAXBElement(q,WSBestillOppgaveRequest::class.java,r)
    }
    private enum class ArenaOppgaveType(val tekst: String) {
        STARTVEDTAK("Start Vedtaksbehandling - automatisk journalfør"),
        BEHENVPERSON("Behandle henvendelse - automatisk journalført")
    }

     private fun oppgaveBeskrivelse(tittel: String, dokumenttitler: List<String>) =
        """
            Hoveddokument: $tittel
            
            
            ${vedleggBeskrivelse(dokumenttitler)}
            
            Registrert dato: ${getFormattedToday()}
            
            Dokumentet er automatisk journalført. Gjennomfør rutinen "Etterkontroll av automatisk journalførte dokumenter"
        """
    private fun vedleggBeskrivelse(vedleggTitler: List<String>): String? {
        val sb = StringBuilder()
        for ( dokLink in vedleggTitler) {
            vedleggTittelAppend(sb, dokLink
                .replace("\\", "\\\\")
                .replace("\"", "\\\""))
        }
        return sb.toString()
    }
    private fun vedleggTittelAppend(sb: StringBuilder, tittel: String?) =
        tittel?.let {
            if (sb.length > "Vedlegg: ".length) {
                sb.append(", ")
            }
            sb.append(tittel.trim { it <= ' ' })
            sb.append("\\n")
        }

     fun saker(fnr: String)  =
        ObjectFactory().createHentSaksInfoListeV2(HentSaksInfoListeRequestV2().apply {
            bruker = Bruker().apply {
                brukerId = fnr
                brukertypeKode = PERSON
            }
            tema = Constants.AAP.uppercase()
            isLukket = false
        })

    fun XMLGregorianCalendar.toLocalDateTime() = toGregorianCalendar().toZonedDateTime().toLocalDateTime()

    private fun getFormattedToday() = SimpleDateFormat("dd.MM.yyyy").format( Date());

    private const val PERSON = "PERSON"

}