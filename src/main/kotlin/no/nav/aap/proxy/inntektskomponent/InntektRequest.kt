package no.nav.aap.proxy.inntektskomponent

import no.nav.aap.api.felles.Fødselsnummer
import java.time.YearMonth

data class InntektRequest(
        val ident: InntektIdent,
        val ainntektsfilter: String,
        val formaal: String,
        val maanedFom: YearMonth,
        val maanedTom: YearMonth) {
    data class InntektIdent(
            val identifikator: Fødselsnummer,
            val aktoerType: AktørType) {
        enum class AktørType { NATURLIG_IDENT }
    }
}