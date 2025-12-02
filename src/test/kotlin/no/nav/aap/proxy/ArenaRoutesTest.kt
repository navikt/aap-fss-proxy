package no.nav.aap.proxy

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.aap.proxy.config.AppConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArenaRoutesTest {
    
    private val testConfig = AppConfig()
    
    @Test
    fun `arena endpoint without proper config causes error`() = testApplication {
        application {
            module(testConfig)
        }
        
        val response = client.get("/arena/vedtak") {
            header("personident", "19819699677")
        }
        
        // Without auth configured and without valid backend, we should get an error
        // Auth is skipped when client ID is empty, so request goes through but backend fails
        assertTrue(response.status.value >= 400, "Expected error status but got ${response.status}")
    }
    
    @Test
    fun `arena endpoint with invalid fnr returns bad request`() = testApplication {
        application {
            module(testConfig)
        }
        
        val response = client.get("/arena/vedtak") {
            header("personident", "invalid")
        }
        
        // Should return bad request due to invalid fødselsnummer format
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
    
    @Test
    fun `arena nyesteaktivesak with invalid fnr returns bad request`() = testApplication {
        application {
            module(testConfig)
        }
        
        val response = client.get("/arena/nyesteaktivesak") {
            header("personident", "12345")
        }
        
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
