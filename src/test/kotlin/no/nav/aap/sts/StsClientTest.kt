package no.nav.aap.sts

import no.nav.aap.createShortCircuitWebClient
import no.nav.aap.createShortCircuitWebClientQueued
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI

class StsClientTest {

    val cfg = StsConfig(URI.create("http://localhost"),"path",true);

    @Language("json")
    private val defaultToken = """
        {
          "access_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJOQVYiLCJpYXQiOjE2MzYzNjkzMDMsImV4cCI6MTY2NzkwNTMwMywiYXVkIjoid3d3Lm5hdi5ubyIsInN1YiI6Im5hdkBuYXYuY29tIn0.Z2PGP0ATOGwzklthb2umsbnI2CHx1HPPDddIpnyKw8c",
          "token_type": "Bearer",
          "expires_in": 3600
        }
    """.trimIndent()

    @Language("json")
    private val shortLivedToken = """
        {
          "access_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJOQVYgdGVzdCIsImlhdCI6MTYzNjM2NjgxOCwiZXhwIjoxNjY3OTAyODE4LCJhdWQiOiJ3d3cubmF2Lm5vIiwic3ViIjoiMTExMTExMTExMSJ9.eTGkBsYMNKY7N9wX9xnY6jUTOSat4oxPEWU4wCeYsHQ",
          "token_type": "Bearer",
          "expires_in": 1
        }
    """.trimIndent()

    @Test
    fun `henter ut riktig token`() {
        val stsClient = StsClient(createShortCircuitWebClient(defaultToken),cfg)
        val token = stsClient.oidcToken()
        assertEquals("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJOQVYiLCJpYXQiOjE2MzYzNjkzMDMsImV4cCI6MTY2NzkwNTMwMywiYXVkIjoid3d3Lm5hdi5ubyIsInN1YiI6Im5hdkBuYXYuY29tIn0.Z2PGP0ATOGwzklthb2umsbnI2CHx1HPPDddIpnyKw8c", token)
    }


    @Test
    fun `refresh token riktig`() {
        val stsDefaultClient = StsClient(createShortCircuitWebClientQueued(shortLivedToken, defaultToken),cfg)
        val token1 = stsDefaultClient.oidcToken()
        val token2 = stsDefaultClient.oidcToken()

        assertEquals("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJOQVYgdGVzdCIsImlhdCI6MTYzNjM2NjgxOCwiZXhwIjoxNjY3OTAyODE4LCJhdWQiOiJ3d3cubmF2Lm5vIiwic3ViIjoiMTExMTExMTExMSJ9.eTGkBsYMNKY7N9wX9xnY6jUTOSat4oxPEWU4wCeYsHQ", token1)
        assertEquals("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJOQVYiLCJpYXQiOjE2MzYzNjkzMDMsImV4cCI6MTY2NzkwNTMwMywiYXVkIjoid3d3Lm5hdi5ubyIsInN1YiI6Im5hdkBuYXYuY29tIn0.Z2PGP0ATOGwzklthb2umsbnI2CHx1HPPDddIpnyKw8c", token2)
    }

    @Test
    fun `token blir cachet`() {
        val stsDefaultClient = StsClient(createShortCircuitWebClientQueued(defaultToken, shortLivedToken),cfg)
        val token1 = stsDefaultClient.oidcToken()
        val token2 = stsDefaultClient.oidcToken()
        assertEquals(token1, token2)
    }
}