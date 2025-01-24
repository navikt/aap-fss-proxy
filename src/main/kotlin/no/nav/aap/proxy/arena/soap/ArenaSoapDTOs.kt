package no.nav.aap.proxy.arena.soap

import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.GregorianCalendar
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.proxy.arena.generated.oppgave.WSBestillOppgaveRequest
import no.nav.aap.proxy.arena.generated.oppgave.WSOppgave
import no.nav.aap.proxy.arena.generated.oppgave.WSOppgavetype
import no.nav.aap.proxy.arena.generated.oppgave.WSPerson
import no.nav.aap.proxy.arena.generated.oppgave.WSPrioritet
import no.nav.aap.proxy.arena.generated.oppgave.WSTema
import no.nav.aap.proxy.arena.generated.sak.Bruker
import no.nav.aap.proxy.arena.generated.sak.HentSaksInfoListeRequestV2
import no.nav.aap.proxy.arena.generated.sak.ObjectFactory
import no.nav.aap.util.Constants.AAP

object ArenaDTOs {

    private const val PERSON = "PERSON"
    private val AAP_TEMA = WSTema().apply { value = AAP.uppercase() }
    private val HØY_PRIORITET = WSPrioritet().apply { value = "HOY" }

    fun oppgaveReq(params : ArenaOpprettOppgaveParams) =
        WSBestillOppgaveRequest().apply {
            oppgavetype = WSOppgavetype().apply { value = params.oppgaveType.name }
            oppgave = oppgave(params)
        }

    fun sakerReq(fnr : String) =
        ObjectFactory().createHentSaksInfoListeV2(HentSaksInfoListeRequestV2().apply {
            bruker = bruker(fnr)
            tema = AAP.uppercase()
            isLukket = false
        })

    fun XMLGregorianCalendar.toLocalDateTime() = toGregorianCalendar().toZonedDateTime().toLocalDateTime()

    private fun idag() = SimpleDateFormat("dd.MM.yyyy").format(Date())

    private fun bruker(fnr : String) = Bruker().apply {
        brukerId = fnr
        brukertypeKode = PERSON
    }

    private fun person(fnr : Fødselsnummer) = WSPerson().apply { ident = fnr.fnr }
    private fun oppgave(params : ArenaOpprettOppgaveParams) = WSOppgave().apply {
        tema = AAP_TEMA
        bruker = person(params.fnr)
        tilleggsinformasjon = oppgaveBeskrivelse(params.tittel, params.titler)
        behandlendeEnhetId = params.enhet
        prioritet = HØY_PRIORITET
        beskrivelse = params.oppgaveType.tekst
        frist = DatatypeFactory.newInstance()
            .newXMLGregorianCalendar(GregorianCalendar.from(ZonedDateTime.now().toInstant().atZone(
                ZoneId.of("Europe/Oslo"))))

    }

    private fun oppgaveBeskrivelse(tittel : String, dokumentTitler : List<String>) =
        StringBuilder()
            .append ( "Hoveddokument: $tittel" )
            .append ( "\n\n" )
            .append ( vedleggBeskrivelse(dokumentTitler) )
            .append ( "\n" )
            .append ( "Registrert dato: ${idag()}" )
            .append ( "\n" )
            .append ( "Dokumentet er automatisk journalført. Gjennomfør rutinen \"Etterkontroll av automatisk journalførte dokumenter\"." )
            .toString()


    private fun vedleggBeskrivelse(vedleggTitler : List<String>) : String {
        val sb = StringBuilder()
        for (dokLink in vedleggTitler) {
            vedleggTittelAppend(sb, dokLink
                .replace("\\", "\\\\")
                .replace("\"", "\\\""))
        }
        if (vedleggTitler.size > 0) { sb.append("\n") }
        return sb.toString()
    }

    private fun vedleggTittelAppend(sb : StringBuilder, tittel : String?) =
        tittel?.let {
            if (sb.length > 0) {
                sb.append(",\n")
            }
            sb.append(tittel.trim { it <= ' ' })
        }
}

data class ArenaOpprettOppgaveParams(
    val fnr : Fødselsnummer,
    val enhet : String,
    val tittel : String,
    val titler : List<String> = emptyList(),
    val oppgaveType: ArenaOppgaveType
)

data class OpprettetOppgave(
    val oppgaveId : String,
    val arenaSakId : String?
) {
    companion object {

        val EMPTY = OpprettetOppgave("0", "0")
    }
}

enum class ArenaOppgaveType(val tekst : String) {
    STARTVEDTAK("Start Vedtaksbehandling - automatisk journalført"),
    BEHENVPERSON("Behandle henvendelse - automatisk journalført")
}

data class BehandleKjoerelisteOgOpprettOppgaveRequest(
    val journalpostId: String
)
data class ArenaSakId(val arenaSakId: String) {
    companion object { val EMPTY = ArenaSakId("0") }
}