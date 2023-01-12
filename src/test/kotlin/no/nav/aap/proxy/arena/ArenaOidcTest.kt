package no.nav.aap.proxy.arena

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.aap.proxy.createShortCircuitWebClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ContextConfiguration
import java.net.URI

@JsonTest
@ContextConfiguration(classes = [ObjectMapper::class])
class ArenaOidcTest {

    @Test
    fun `Henter token fra Arena`() {
        val arenaOidcClient = ArenaOIDCClient(createShortCircuitWebClient(defaultToken), cfg)
        Assertions.assertEquals(longLived, arenaOidcClient.oidcToken())
    }

    companion object {
        val cfg = ArenaOIDCConfig(URI.create("http://localhost"),true)
        const val longLived =  "retwretwertre"
        const val shortLived =  "etyrtyertytry"
        val defaultToken = """
        {
          "access_token": "${longLived}",
          "token_type": "Bearer",
          "expires_in": 3600
        }
    """.trimIndent()

        val shortLivedToken = """
        {
          "access_token": "${shortLived}",
          "token_type": "Bearer",
          "expires_in": 1
        }
    """.trimIndent()
    }

}