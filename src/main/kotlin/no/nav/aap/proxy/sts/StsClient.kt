package no.nav.aap.proxy.sts

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
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
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

private val log = LoggerFactory.getLogger("StsClient")

class StsClient(private val config: AppConfig) {
    private val stsConfig = config.sts
    private val serviceuser = config.serviceuser
    
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
            url(stsConfig.baseUri)
        }
    }
    
    @Volatile
    private var cachedToken: OidcToken? = null
    
    fun oidcToken(): String = runBlocking {
        val token = cachedToken
        if (token != null && !token.hasExpired()) {
            return@runBlocking token.accessToken
        }
        
        log.trace("Fornyer STS token")
        val newToken = getToken()
        cachedToken = newToken
        log.info("STS oppslag OK, utgår om ${newToken.expiresIn}s")
        newToken.accessToken
    }
    
    private suspend fun getToken(): OidcToken {
        try {
            return httpClient.get(stsConfig.tokenPath) {
                header(HttpHeaders.Authorization, "Basic ${serviceuser.credentials}")
                parameter("grant_type", "client_credentials")
                parameter("scope", "openid")
            }.body<OidcToken>()
        } catch (e: Exception) {
            log.warn("STS oppslag feilet", e)
            throw IrrecoverableIntegrationException("STS token oppslag feilet: ${e.message}", e)
        }
    }
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class OidcToken(
    val accessToken: String,
    val tokenType: String?,
    val expiresIn: Int?
) {
    private val createdTime = LocalDateTime.now()
    
    fun hasExpired(): Boolean {
        val expiry = expiresIn ?: return true
        val expirationTime = createdTime.plusSeconds(expiry.toLong())
        return LocalDateTime.now().minusSeconds(30).isAfter(expirationTime)
    }
}
