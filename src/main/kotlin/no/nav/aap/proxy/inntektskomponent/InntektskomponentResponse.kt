package no.nav.aap.proxy.inntektskomponent

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.OrgNummer
import java.time.YearMonth

data class InntektskomponentResponse(
    val arbeidsInntektMaaned: List<Måned>
)

data class Måned(
    val årMåned: YearMonth,
    val arbeidsforholdliste: List<Arbeidsforhold>,
    val inntektsliste: List<Inntekt>
)
data class Arbeidsforhold(
    val type: String?,
    val orgnummer: OrgNummer?
)
data class Inntekt(
    val beløp: Double,
    val inntektstype: Inntektstype,
    val orgnummer: String?,
    val fødselsnummer: Fødselsnummer?,
    val aktørId: String?,
    val beskrivelse: String?,
    val fordel: String?
)

enum class Inntektstype {
    LOENNSINNTEKT,
    NAERINGSINNTEKT,
    PENSJON_ELLER_TRYGD,
    YTELSE_FRA_OFFENTLIGE
}