package no.nav.aap.proxy

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.aap.proxy.config.AppConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HealthRoutesTest {
    
    private val testConfig = AppConfig()
    
    @Test
    fun `isAlive returns ALIVE`() = testApplication {
        application {
            module(testConfig)
        }
        
        val response = client.get("/internal/isalive")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("ALIVE", response.bodyAsText())
    }
    
    @Test
    fun `isReady returns READY`() = testApplication {
        application {
            module(testConfig)
        }
        
        val response = client.get("/internal/isready")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("READY", response.bodyAsText())
    }
    
    @Test
    fun `prometheus endpoint returns metrics`() = testApplication {
        application {
            module(testConfig)
        }
        
        val response = client.get("/internal/prometheus")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("jvm_") || response.bodyAsText().isNotEmpty())
    }
}
