package no.nav.aap.proxy.arena

import java.net.URI
import no.nav.aap.proxy.arena.ArenaConfig.Companion.ARENA
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(ARENA)
class ArenaConfig(baseUri: URI,
                  @DefaultValue("/aap/sisteVedtak") val path: String,
                  @DefaultValue("true") enabled: Boolean, retry: RetryConfig = RetryConfig.DEFAULT): AbstractRestConfig(baseUri,"",
        "arena",enabled, retry) {

    companion object {
        const val ARENA = "arena"
    }
}