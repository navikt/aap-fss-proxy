package no.nav.aap.proxy.arena

import java.net.URI
import no.nav.aap.proxy.arena.ArenaRestConfig.Companion.ARENA
import no.nav.aap.proxy.arena.ArenaSoapConfig.Companion.ARENASOAP
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(ARENASOAP)
class ArenaSoapConfig(val baseUri: String,
                      @NestedConfigurationProperty val credentials: ArenaCredentials,
                      @DefaultValue(SAKER) val saker: String,
                      @DefaultValue("true") enabled: Boolean) {

    val sakerURI = "$baseUri$saker"

    companion object {
        const val SAKER = "/ArenaSakVedtakService"
        const val ARENASOAP = "arenasoap"
    }
}