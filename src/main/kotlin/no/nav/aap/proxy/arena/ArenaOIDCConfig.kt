package no.nav.aap.proxy.arena

import java.net.URI
import no.nav.aap.proxy.arena.ArenaOIDCConfig.Companion.ARENAOIDC
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(ARENAOIDC)
class ArenaOIDCConfig(baseUri: URI,
                      @DefaultValue("true") enabled: Boolean, retry: RetryConfig = DEFAULT): AbstractRestConfig(baseUri,"",
        ARENAOIDC,enabled, retry) {
    companion object {
        const val ARENAOIDC = "arenaoidc"
    }
}