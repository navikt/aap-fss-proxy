package no.nav.aap.proxy.inntektskomponent

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.runBlocking
import no.nav.aap.proxy.config.AppConfig
import no.nav.aap.proxy.plugins.IrrecoverableIntegrationException
import no.nav.aap.proxy.plugins.NAV_CALL_ID
import no.nav.aap.proxy.plugins.NAV_CONSUMER_ID
import no.nav.aap.proxy.plugins.CallIdUtil
import no.nav.aap.proxy.sts.StsClient
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("InntektClient")

class InntektClient(private val config: AppConfig) {
    private val inntektConfig = config.inntektskomponent
    private val stsClient = StsClient(config)
    
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
        defaultRequest {
            url(inntektConfig.baseUri)
        }
    }
    
    fun getInntekt(request: InntektRequest): InntektResponse = runBlocking {
        val token = stsClient.oidcToken()
        
        try {
            httpClient.post(inntektConfig.path) {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                header(NAV_CALL_ID, CallIdUtil.callId())
                header(NAV_CONSUMER_ID, "aap")
                setBody(request)
            }.body<InntektResponse>()
        } catch (e: Exception) {
            log.warn("Inntektsoppslag feilet", e)
            throw IrrecoverableIntegrationException("Inntekt oppslag feilet: ${e.message}", e)
        }
    }
}
