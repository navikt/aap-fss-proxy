package no.nav.aap.proxy.arena

import java.nio.charset.StandardCharsets
import java.nio.charset.StandardCharsets.*
import no.nav.aap.proxy.arena.ArenaUserConfig.Companion.ARENACLIENT
import no.nav.aap.util.StringExtensions.encode
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(ARENACLIENT)
data class ArenaUserConfig(val id: String, val secret: String) {
    val credentials = "$id:$secret".toByteArray(UTF_8).encode()

    companion object {
        const val ARENACLIENT = "arenaclient"
    }
}