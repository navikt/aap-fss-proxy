package no.nav.aap.sts

import no.nav.aap.createShortCircuitWebClient
import no.nav.aap.createShortCircuitWebClientQueued
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StsClientTest {

    @Test
    fun `henter ut riktig token`() {
        val stsClient = StsClient(createShortCircuitWebClient(defaultToken))
        val token = stsClient.oidcToken()

        assertEquals("default access token", token)
    }

    @Test
    fun `refresh token riktig`() {
        val stsDefaultClient = StsClient(createShortCircuitWebClientQueued(shortLivedToken, defaultToken))
        val token1 = stsDefaultClient.oidcToken()
        val token2 = stsDefaultClient.oidcToken()

        assertEquals("short lived token", token1)
        assertEquals("default access token", token2)
    }

    @Test
    fun `token blir cachet`() {
        val stsDefaultClient = StsClient(createShortCircuitWebClientQueued(defaultToken, shortLivedToken))
        val token1 = stsDefaultClient.oidcToken()
        val token2 = stsDefaultClient.oidcToken()

        assertEquals(token1, token2)
    }

    @Language("json")
    private val defaultToken = """
        {
          "access_token": "default access token",
          "token_type": "Bearer",
          "expires_in": 3600
        }
    """.trimIndent()

    @Language("json")
    private val shortLivedToken = """
        {
          "access_token": "short lived token",
          "token_type": "Bearer",
          "expires_in": 1
        }
    """.trimIndent()
}
