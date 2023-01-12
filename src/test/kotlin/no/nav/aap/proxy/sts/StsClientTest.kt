package no.nav.aap.proxy.sts

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.aap.proxy.createShortCircuitWebClient
import no.nav.aap.proxy.createShortCircuitWebClientQueued
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ContextConfiguration
import java.net.URI

@JsonTest
@ContextConfiguration(classes = [ObjectMapper::class])
class StsClientTest {

    @Autowired
    private lateinit var mapper: ObjectMapper
    @Test
    fun `henter ut riktig token`() {
        val stsClient = StsClient(createShortCircuitWebClient(defaultToken),cfg)
        assertEquals(longLived, stsClient.oidcToken())
    }
    @Test
    @Disabled
    fun `refresh token riktig`() {
        val stsDefaultClient = StsClient(createShortCircuitWebClientQueued(shortLivedToken, defaultToken),cfg)
        assertEquals(shortLived, stsDefaultClient.oidcToken())
        assertEquals(longLived, stsDefaultClient.oidcToken())
    }

    @Test
    @Disabled
    fun `token blir cachet`() {
        val stsDefaultClient = StsClient(createShortCircuitWebClientQueued(defaultToken, shortLivedToken),cfg)
        assertEquals(stsDefaultClient.oidcToken(), stsDefaultClient.oidcToken())
    }

    @Test
    fun `deserialisering av snake case respons`() {
        val token = mapper.readValue(defaultToken,OidcToken::class.java)
        assertEquals(token.tokenType,"Bearer")
        assertEquals(token.expiresIn,3600)
        assertEquals(token.accessToken!!.tokenAsString, longLived)
    }

    companion object {
        val cfg = StsConfig(URI.create("http://localhost"),"path","token",true)
        const val longLived =  "yetruywetr"
        const val shortLived =  "akdjslfhkdsfh"
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