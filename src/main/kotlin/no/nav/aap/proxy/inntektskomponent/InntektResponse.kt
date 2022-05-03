package no.nav.aap.proxy.inntektskomponent

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.aap.api.felles.OrgNummer
import java.time.YearMonth

data class InntektResponse(val arbeidsInntektMaaned: List<Måned>) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Måned(val årMåned: YearMonth,  val inntektsliste: List<Inntekt>) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Inntekt(val beløp: Double, val orgnummer: OrgNummer?)
    }
}