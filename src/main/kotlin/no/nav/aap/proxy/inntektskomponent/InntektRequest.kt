package no.nav.aap.proxy.inntektskomponent

import java.time.YearMonth

data class InntektskomponentRequest(
    val ident: InntektskomponentIdent,
    val ainntektsfilter: String,
    val formaal: String,
    val maanedFom: YearMonth,
    val maanedTom: YearMonth
)

data class InntektskomponentIdent(
    val identifikator: String,
    val aktoerType: String
)