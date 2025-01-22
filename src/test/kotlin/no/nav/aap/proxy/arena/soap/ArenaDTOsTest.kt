package no.nav.aap.proxy.arena.soap

import no.nav.aap.api.felles.Fødselsnummer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class ArenaDTOsTest {


    @Test
    fun `Konstruksjon av tilleggsinformasjon`() {
        val params = ArenaOpprettOppgaveParams(
            fnr = Fødselsnummer("10516335918"),
            oppgaveType = ArenaOppgaveType.STARTVEDTAK,
            enhet = "23434",
            tittel = "Søknad om arbeidsavklaringspenger",
            titler = listOf("Vedlegg 1", "Vedlegg 2")
        )
        val request = ArenaDTOs.oppgaveReq(params)

        Assertions.assertThat(request.oppgave.tilleggsinformasjon)
            .isEqualTo("Hoveddokument: Søknad om arbeidsavklaringspenger\\n\\nVedlegg 1,\\nVedlegg 2\\nRegistrert dato: 22.01.2025\\nDokumentet er automatisk journalført. Gjennomfør rutinen \\\"Etterkontroll av automatisk journalførte dokumenter\\\".")
    }
}
