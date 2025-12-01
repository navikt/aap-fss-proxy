package no.nav.aap.api.felles

import com.fasterxml.jackson.annotation.JsonValue
import no.nav.aap.util.StringExtensions.decap
import no.nav.aap.util.StringExtensions.partialMask

data class Fødselsnummer(@get:JsonValue val fnr : String) {
    init {
        require(fnr.length == 11) { "Fødselsnummer $fnr er ikke 11 siffer" }
        require(mod11(W1, fnr) == fnr[9] - '0') { "Første kontrollsiffer $fnr[9] ikke validert" }
        require(mod11(W2, fnr) == fnr[10] - '0') { "Andre kontrollsiffer $fnr[10] ikke validert" }
    }

    companion object {

        private val W1 = intArrayOf(2, 5, 4, 9, 8, 1, 6, 7, 3)
        private val W2 = intArrayOf(2, 3, 4, 5, 6, 7, 2, 3, 4, 5)

        private fun mod11(weights : IntArray, fnr : String) =
            with(weights.indices.sumOf { weights[it] * (fnr[(weights.size - 1 - it)] - '0') } % 11) {
                when (this) {
                    0 -> 0
                    1 -> throw IllegalArgumentException(fnr)
                    else -> 11 - this
                }
            }
    }

    override fun toString() = "${javaClass.simpleName} [fnr=${fnr.partialMask()}]"
}