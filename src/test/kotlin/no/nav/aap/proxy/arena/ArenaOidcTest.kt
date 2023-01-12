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
        const val longLived =  "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJOQVYiLCJpYXQiOjE2MzYzNjkzMDMsImV4cCI6MTY2NzkwNTMwMywiYXVkIjoid3d3Lm5hdi5ubyIsInN1YiI6Im5hdkBuYXYuY29tIn0.Z2PGP0ATOGwzklthb2umsbnI2CHx1HPPDddIpnyKw8c"
        const val shortLived =  "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJOQVYgdGVzdCIsImlhdCI6MTYzNjM2NjgxOCwiZXhwIjoxNjY3OTAyODE4LCJhdWQiOiJ3d3cubmF2Lm5vIiwic3ViIjoiMTExMTExMTExMSJ9.eTGkBsYMNKY7N9wX9xnY6jUTOSat4oxPEWU4wCeYsHQ"
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