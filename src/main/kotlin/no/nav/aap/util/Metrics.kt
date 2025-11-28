package no.nav.aap.util


import io.micrometer.core.instrument.Counter.builder
import io.micrometer.core.instrument.Metrics.globalRegistry

object Metrikker {

    fun inc(navn: String, vararg tags: String) =
        inc(navn, tags.toList().chunked(2) { Pair(it[0], it[1]) })

    fun inc(navn: String, tags: List<Pair<String, Any>>) =
        builder(navn).apply {
            tags.forEach { tag(it.first, "${it.second}") }
            register(globalRegistry).increment()
        }
}