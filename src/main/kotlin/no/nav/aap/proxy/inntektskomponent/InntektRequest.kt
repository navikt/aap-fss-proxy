package no.nav.aap.proxy.inntektskomponent

import java.time.YearMonth

data class InntektRequest(
        val ident: InntektIdent,
        val ainntektsfilter: String,
        val formaal: String,
        val maanedFom: YearMonth,
        val maanedTom: YearMonth
                         )

data class InntektIdent(
    val identifikator: String,
    val aktoerType: String
                       )