package no.nav.aap.proxy.arena

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URI
import no.nav.aap.proxy.arena.rest.ArenaVedtakRestConfig
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ContextConfiguration

@JsonTest
@Disabled
@ContextConfiguration(classes = [ObjectMapper::class])
class ArenaOidcTest {

    @Test
    fun `Henter token fra Arena`() {
     //   val arenaOidcClient = ArenaOIDCClient(createShortCircuitWebClient(defaultToken), cfg)
     //   Assertions.assertEquals(longLived, arenaOidcClient.oidcToken())
    }

    companion object {
        val cfg = ArenaVedtakRestConfig(URI.create("http://localhost"), "/jalla","/token","jalla",ArenaCredentials("user","pasword"),true)
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