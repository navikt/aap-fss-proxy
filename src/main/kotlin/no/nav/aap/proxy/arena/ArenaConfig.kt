package no.nav.aap.proxy.arena

import java.net.URI
import no.nav.aap.proxy.arena.ArenaConfig.Companion.ARENA
import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(ARENA)
class ArenaConfig(baseUri: URI,
                  @DefaultValue(DEFAULT_PATH) val path: String,
                  @DefaultValue("true") enabled: Boolean): AbstractRestConfig(baseUri,"", ARENA,enabled)  {

    companion object {
        const val DEFAULT_PATH = "aap/sisteVedtak"
        const val ARENA = "arena"
    }
}