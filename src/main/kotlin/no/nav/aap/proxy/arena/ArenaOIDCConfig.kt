package no.nav.aap.proxy.arena

import java.net.URI
import no.nav.aap.proxy.arena.ArenaOIDCConfig.Companion.ARENAOIDC
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import org.aspectj.weaver.tools.cache.SimpleCacheFactory.enabled
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(ARENAOIDC)
class ArenaOIDCConfig(baseUri: URI,
                      @DefaultValue(TOKEN_PATH) val tokenPath: String,
                      @DefaultValue("true") enabled: Boolean): AbstractRestConfig(baseUri,"", ARENAOIDC,enabled, DEFAULT) {
    companion object {
        private const val TOKEN_PATH = "/oauth/token"
        const val ARENAOIDC = "arenaoidc"
    }
}