package no.nav.aap.proxy.inntektskomponent

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.OrgNummer
import java.time.YearMonth

data class InntektRequest(val ident: InntektIdent, val ainntektsfilter: String, val formaal: String, val maanedFom: YearMonth, val maanedTom: YearMonth) {
    data class InntektIdent(
            val identifikator: Fødselsnummer,
            val aktoerType: AktørType) {
        enum class AktørType { NATURLIG_IDENT }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class InntektResponse(val arbeidsInntektMaaned: List<Måned>) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Måned(val aarMaaned: YearMonth,  val arbeidsInntektInformasjon: ArbeidsInntektInformasjon)

    data class ArbeidsInntektInformasjon(val inntektListe: List<Inntekt>) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Inntekt(val beloep: Double)
    }
}