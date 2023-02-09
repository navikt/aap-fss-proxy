package no.nav.aap.proxy.arena

import java.nio.charset.StandardCharsets
import java.util.*
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("arenaclient")
data class ArenaUserConfig(val id: String, val secret: String) {
    val credentials = Base64.getEncoder().encodeToString("$id:$secret".toByteArray(StandardCharsets.UTF_8))
}