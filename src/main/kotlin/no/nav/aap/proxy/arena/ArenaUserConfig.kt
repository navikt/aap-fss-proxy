package no.nav.aap.proxy.arena

import java.nio.charset.StandardCharsets
import java.nio.charset.StandardCharsets.*
import java.util.*
import no.nav.aap.util.StringExtensions.encode
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("arenaclient")
data class ArenaUserConfig(val id: String, val secret: String) {
    val credentials = "Basic ${"$id:$secret".encode()}"
}