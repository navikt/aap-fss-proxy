package no.nav.aap.joark

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class DokumentInfoId(val dokumentInfoId: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class JoarkResponse(val journalpostId: String, val journalpostferdigstilt: Boolean, val dokumenter: List<DokumentInfoId> )