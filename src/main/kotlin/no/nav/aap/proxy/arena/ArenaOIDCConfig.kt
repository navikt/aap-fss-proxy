package no.nav.aap.proxy.arena

import java.net.URI
import no.nav.aap.proxy.arena.ArenaOIDCConfig.Companion.ARENAOIDC
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import no.nav.aap.util.StringExtensions.encode
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(ARENAOIDC)
class ArenaOIDCConfig(baseUri: URI,
                      @DefaultValue(TOKEN_PATH) val tokenPath: String,
                      username: String,
                      password: String,
                      @DefaultValue("true") enabled: Boolean): AbstractRestConfig(baseUri,"", ARENAOIDC,enabled, DEFAULT) {

    val credentials = "Basic ${"$username:$password".encode()}"

    companion object {
        private const val TOKEN_PATH = "/oauth/token"
        const val ARENAOIDC = "arenaoidc"
    }
}