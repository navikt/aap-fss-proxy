package no.nav.aap.proxy.arena

import no.nav.aap.api.felles.Periode

data class ArenaResponse (
    val aapSak: AapSak,
    val ytelsesvedtak: Ytelsesvedtak,
        )

data class AapSak(
    val saksnummer: String,
    val saksstatus: String,
    val restOrdPeriode: Int,
    val restUnntakPeriode: Int,
)

data class Ytelsesvedtak(
        val vedtakstype: String,
        val aktivitetsfase: String,
        val vedtaksperiode: Periode,
        val beslutningsdato: String,
        val status: String,
        val unntaksvedtak: String,
        val sisteMeldekort: SisteMeldekort,
        val meldedato: String,
        val utbetaling: String
)

data class SisteMeldekort(
    val meldeperiode: Periode
)