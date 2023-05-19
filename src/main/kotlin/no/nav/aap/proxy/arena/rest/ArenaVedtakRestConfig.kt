package no.nav.aap.proxy.arena.rest

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import no.nav.aap.proxy.arena.ArenaCredentials
import no.nav.aap.proxy.arena.rest.ArenaVedtakRestConfig.Companion.ARENA
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import no.nav.aap.util.StringExtensions.encode

@ConfigurationProperties(ARENA)
class ArenaVedtakRestConfig(baseUri : URI,
                            val path : String = SISTE_VEDTAK,
                            val tokenPath : String = TOKEN_PATH,
                            pingPath : String = PING_PATH,
                            @NestedConfigurationProperty val credentials : ArenaCredentials,
                            enabled : Boolean = true) : AbstractRestConfig(baseUri, pingPath, ARENA, enabled, DEFAULT) {

    val asBasic = "Basic ${"${credentials.id}:${credentials.secret}".encode()}"

    companion object {

        private const val TOKEN_PATH = "/oauth/token"
        const val SISTE_VEDTAK = "/v1/aap/sisteVedtak"
        const val PING_PATH = "v1/test/ping"
        const val ARENA = "arena"
        const val ARENAOIDC = "arenaoidc"
    }
}