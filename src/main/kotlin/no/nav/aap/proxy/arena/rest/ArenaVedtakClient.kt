package no.nav.aap.proxy.arena.rest

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import kotlinx.coroutines.runBlocking
import no.nav.aap.proxy.config.AppConfig
import no.nav.aap.proxy.plugins.IrrecoverableIntegrationException
import no.nav.aap.proxy.plugins.NAV_CALL_ID
import no.nav.aap.proxy.plugins.NAV_CONSUMER_ID
import no.nav.aap.proxy.plugins.CallIdUtil
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.Base64

private val log = LoggerFactory.getLogger("ArenaVedtakClient")

class ArenaVedtakClient(private val config: AppConfig) {
    private val arenaConfig = config.arena
    
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
            url(arenaConfig.baseUri)
        }
    }
    
    private var cachedToken: ArenaOidcToken? = null
    
    fun sisteVedtak(fnr: String): ByteArray = runBlocking {
        val token = getOidcToken()
        
        try {
            httpClient.get(arenaConfig.path) {
                header("fnr", fnr)
                header(HttpHeaders.Authorization, "Bearer $token")
                header(NAV_CALL_ID, CallIdUtil.callId())
                header(NAV_CONSUMER_ID, CallIdUtil.consumerId("aap-fss-proxy"))
            }.body<ByteArray>()
        } catch (e: Exception) {
            log.warn("Arenaoppslag feilet", e)
            throw IrrecoverableIntegrationException("Arena vedtak oppslag feilet: ${e.message}", e)
        }
    }
    
    private suspend fun getOidcToken(): String {
        val token = cachedToken
        if (token != null && !token.hasExpired()) {
            return token.accessToken
        }
        
        log.info("Fornyer Arena OIDC token")
        val basic = Base64.getEncoder().encodeToString(
            "${arenaConfig.credentials.id}:${arenaConfig.credentials.secret}".toByteArray()
        )
        
        val newToken = try {
            httpClient.post(arenaConfig.tokenPath) {
                header(HttpHeaders.Authorization, "Basic $basic")
                header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
                setBody("grant_type=client_credentials")
            }.body<ArenaOidcToken>()
        } catch (e: Exception) {
            log.warn("Arena OIDC oppslag feilet!", e)
            throw IrrecoverableIntegrationException("Arena OIDC token oppslag feilet: ${e.message}", e)
        }
        
        log.trace("Arena OIDC oppslag OK, utgår om ${newToken.expiresIn}s")
        cachedToken = newToken
        return newToken.accessToken
    }
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ArenaOidcToken(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Int
) {
    private val createdTime = LocalDateTime.now()
    
    fun hasExpired(): Boolean {
        val expirationTime = createdTime.plusSeconds(expiresIn.toLong())
        return LocalDateTime.now().minusSeconds(30).isAfter(expirationTime)
    }
}
