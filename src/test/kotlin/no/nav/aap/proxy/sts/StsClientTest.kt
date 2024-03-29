package no.nav.aap.proxy.sts

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URI
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ContextConfiguration
import no.nav.aap.proxy.createShortCircuitWebClient
import no.nav.aap.proxy.createShortCircuitWebClientQueued

@JsonTest
@ContextConfiguration(classes = [ObjectMapper::class])
class StsClientTest {

    @Autowired
    private lateinit var mapper : ObjectMapper

    @Test
    @Disabled
    fun `henter ut riktig token`() {
        assertEquals(longLived, StsClient(createShortCircuitWebClient(defaultToken), cfg).oidcToken())
    }

    @Test
    @Disabled
    fun `refresh token riktig`() {
        val stsDefaultClient = StsClient(createShortCircuitWebClientQueued(shortLivedToken, defaultToken), cfg)
        assertEquals(shortLived, stsDefaultClient.oidcToken())
        assertEquals(longLived, stsDefaultClient.oidcToken())
    }

    @Test
    @Disabled
    fun `token blir cachet`() {
        val stsDefaultClient = StsClient(createShortCircuitWebClientQueued(defaultToken, shortLivedToken), cfg)
        assertEquals(stsDefaultClient.oidcToken(), stsDefaultClient.oidcToken())
    }

    @Test
    fun `deserialisering av snake case respons`() {
        val token = mapper.readValue(defaultToken, OidcToken::class.java)
        assertEquals(token.tokenType, "Bearer")
        assertEquals(token.expiresIn, 3600)
        assertEquals(token.accessToken!!.tokenAsString, longLived)
    }

    companion object {

        val cfg = StsConfig(URI.create("http://localhost"), "path", "token", true)
        const val longLived =
            "eyJ0eXAi   OiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJOQVYiLCJpYXQiOjE2MzYzNjkzMDMsImV4cCI6MTY2NzkwNTMwMywiYXVkIjoid3d3Lm5hdi5ubyIsInN1YiI6Im5hdkBuYXYuY29tIn0.Z2PGP0ATOGwzklthb2umsbnI2CHx1HPPDddIpnyKw8c"
        const val shortLived =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJOQVYgdGVzdCIsImlhdCI6MTYzNjM2NjgxOCwiZXhwIjoxNjY3OTAyODE4LCJhdWQiOiJ3d3cubmF2Lm5vIiwic3ViIjoiMTExMTExMTExMSJ9.eTGkBsYMNKY7N9wX9xnY6jUTOSat4oxPEWU4wCeYsHQ"
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