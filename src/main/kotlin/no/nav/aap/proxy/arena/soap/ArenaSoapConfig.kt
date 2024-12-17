package no.nav.aap.proxy.arena.soap

import no.nav.aap.proxy.arena.ArenaCredentials
import no.nav.aap.proxy.arena.soap.ArenaSoapConfig.Companion.ARENASOAP
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import java.net.URI

@ConfigurationProperties(ARENASOAP)
class ArenaSoapConfig(val baseUri : String,
                      val oppgaveUri : String,
                      val behandleSakOgAktivitetUri : String,
                      @NestedConfigurationProperty val sts : ArenaSTSConfig,
                      @NestedConfigurationProperty val credentials : ArenaCredentials,
                      val saker : String = SAKER,
                      val enabled : Boolean = true) {

    val sakerURI = "$baseUri$saker"

    companion object {

        const val SAK = "sak"
        const val SAKER = "/ArenaSakVedtakService"
        const val ARENASOAP = "arenasoap"
    }

    data class ArenaSTSConfig(val url : URI, val username : String, val password : String)
}