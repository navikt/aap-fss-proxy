package no.nav.aap.proxy.inntektskomponent

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.YearMonth
import no.nav.aap.api.felles.Fødselsnummer

data class InntektRequest(val ident: InntektIdent, val ainntektsfilter: String, val formaal: String, val maanedFom: YearMonth, val maanedTom: YearMonth)

@JsonIgnoreProperties(ignoreUnknown = true)
data class InntektResponse(val arbeidsInntektMaaned: List<Måned>?, val ident: InntektIdent) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Måned(val aarMaaned: YearMonth,  val arbeidsInntektInformasjon: ArbeidsInntektInformasjon)

    data class ArbeidsInntektInformasjon(val inntektListe: List<Inntekt>) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Inntekt(val beloep: Double)
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class InntektIdent(
    val identifikator: Fødselsnummer,
    val aktoerType: AktørType) {
    enum class AktørType { NATURLIG_IDENT }
}