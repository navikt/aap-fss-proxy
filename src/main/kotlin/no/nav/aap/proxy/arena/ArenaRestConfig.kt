package no.nav.aap.proxy.arena

import java.net.URI
import no.nav.aap.proxy.arena.ArenaRestConfig.Companion.ARENA
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import no.nav.aap.util.StringExtensions.encode
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(ARENA)
class ArenaRestConfig(baseUri: URI,
                      @DefaultValue(SISTE_VEDTAK) val path: String,
                      @DefaultValue(TOKEN_PATH) val tokenPath: String,
                      @NestedConfigurationProperty val credentials: ArenaCredentials,
                      @DefaultValue("true") enabled: Boolean): AbstractRestConfig(baseUri,"", ARENA,enabled, DEFAULT) {

    val asBasic =  "Basic ${"${credentials.id}:${credentials.secret}".encode()}"


    companion object {
        private const val TOKEN_PATH = "/oauth/token"
        const val SISTE_VEDTAK = "/v1/aap/sisteVedtak"
        const val ARENA = "arena"
        const val ARENAOIDC = "arenaoidc"
    }
}