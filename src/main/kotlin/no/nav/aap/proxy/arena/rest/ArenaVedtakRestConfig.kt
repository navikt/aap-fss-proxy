package no.nav.aap.proxy.arena.rest

import java.net.URI
import no.nav.aap.proxy.arena.ArenaCredentials
import no.nav.aap.proxy.arena.rest.ArenaVedtakRestConfig.Companion.ARENA
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import no.nav.aap.util.StringExtensions.encode
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(ARENA)
class ArenaVedtakRestConfig(baseUri: URI,
                            @DefaultValue(SISTE_VEDTAK) val path: String,
                            @DefaultValue(TOKEN_PATH) val tokenPath: String,
                            @DefaultValue(PING_PATH) pingPath: String,
                            @NestedConfigurationProperty val credentials: ArenaCredentials,
                            @DefaultValue("true") enabled: Boolean): AbstractRestConfig(baseUri,pingPath, ARENA,enabled, DEFAULT) {

    val asBasic =  "Basic ${"${credentials.id}:${credentials.secret}".encode()}"


    companion object {
        private const val TOKEN_PATH = "/oauth/token"
        const val SISTE_VEDTAK = "/v1/aap/sisteVedtak"
        const val PING_PATH = "/v1/test/ping"
        const val ARENA = "arena"
        const val ARENAOIDC = "arenaoidc"
    }
}