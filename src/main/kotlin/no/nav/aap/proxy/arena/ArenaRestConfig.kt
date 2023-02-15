package no.nav.aap.proxy.arena

import java.net.URI
import no.nav.aap.proxy.arena.ArenaRestConfig.Companion.ARENA
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(ARENA)
class ArenaRestConfig(baseUri: URI,
                      @DefaultValue(SISTE_VEDTAK) val path: String,
                      @DefaultValue("true") enabled: Boolean): AbstractRestConfig(baseUri,"", ARENA,enabled, DEFAULT) {

    companion object {
        const val SISTE_VEDTAK = "/aap/sisteVedtak"
        const val ARENA = "arena"
    }
}