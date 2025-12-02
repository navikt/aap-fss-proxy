package no.nav.aap.proxy.arena

import com.fasterxml.jackson.annotation.JsonValue

data class Fødselsnummer(@get:JsonValue val fnr: String) {
    init {
        require(fnr.length == 11) { "Fødselsnummer $fnr er ikke 11 siffer" }
        require(mod11(W1, fnr) == fnr[9] - '0') { "Første kontrollsiffer ${fnr[9]} ikke validert" }
        require(mod11(W2, fnr) == fnr[10] - '0') { "Andre kontrollsiffer ${fnr[10]} ikke validert" }
    }

    companion object {
        private val W1 = intArrayOf(2, 5, 4, 9, 8, 1, 6, 7, 3)
        private val W2 = intArrayOf(2, 3, 4, 5, 6, 7, 2, 3, 4, 5)

        private fun mod11(weights: IntArray, fnr: String): Int {
            val sum = weights.indices.sumOf { weights[it] * (fnr[(weights.size - 1 - it)] - '0') } % 11
            return when (sum) {
                0 -> 0
                1 -> throw IllegalArgumentException(fnr)
                else -> 11 - sum
            }
        }
    }

    override fun toString() = "Fødselsnummer [fnr=${fnr.take(6)}*****]"
}

enum class ArenaOppgaveType(val tekst: String) {
    STARTVEDTAK("Start Vedtaksbehandling - automatisk journalført"),
    BEHENVPERSON("Behandle henvendelse - automatisk journalført")
}

data class ArenaOpprettOppgaveParams(
    val fnr: Fødselsnummer,
    val enhet: String,
    val tittel: String,
    val titler: List<String> = emptyList(),
    val oppgaveType: ArenaOppgaveType
)

data class OpprettetOppgave(
    val oppgaveId: String,
    val arenaSakId: String?
) {
    companion object {
        val EMPTY = OpprettetOppgave("0", "0")
    }
}

data class BehandleKjoerelisteOgOpprettOppgaveRequest(
    val journalpostId: String
)

data class ArenaSakId(val arenaSakId: String) {
    companion object {
        val EMPTY = ArenaSakId("0")
    }
}
