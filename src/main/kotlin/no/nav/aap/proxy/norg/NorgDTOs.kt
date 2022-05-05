package no.nav.aap.proxy.norg

data class ArbeidRequest(
    val geografiskOmraade: String,
    val tema: String,
    val behandlingstema: String,
    val skjermet: Boolean,
    val diskresjonskode: String)

data class ArbeidResponse(val enhetNr: String)