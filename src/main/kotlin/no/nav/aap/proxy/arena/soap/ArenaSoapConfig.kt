package no.nav.aap.proxy.arena.soap

import no.nav.aap.proxy.arena.ArenaCredentials
import no.nav.aap.proxy.arena.soap.ArenaSoapConfig.Companion.ARENASOAP
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(ARENASOAP)
class ArenaSoapConfig(val baseUri: String,
                      val oppgaveUri: String,
                      @NestedConfigurationProperty val credentials: ArenaCredentials,
                      @DefaultValue(SAKER) val saker: String,
                      @DefaultValue("true") val enabled: Boolean) {

    val sakerURI = "$baseUri$saker"

    companion object {
        const val SAK = "sak"
        const val SAKER = "/ArenaSakVedtakService"
        const val ARENASOAP = "arenasoap"
    }
}