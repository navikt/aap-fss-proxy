package no.nav.aap.proxy.plugins

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import no.nav.aap.proxy.config.AppConfig
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.concurrent.TimeUnit

const val AAD = "aad"

private val authLog = LoggerFactory.getLogger("Authentication")

fun Application.configureAuthentication(config: AppConfig) {
    val azureConfig = config.azure.app
    
    // Skip auth setup if no client ID configured (for local development)
    if (azureConfig.clientId.isEmpty()) {
        authLog.warn("Azure client ID not configured - authentication disabled")
        install(Authentication) {
            jwt(AAD) {
                skipWhen { true }
            }
        }
        return
    }
    
    val effectiveWellKnownUrl = azureConfig.effectiveWellKnownUrl
    if (effectiveWellKnownUrl.isEmpty()) {
        authLog.warn("Azure well-known URL not configured - authentication disabled")
        install(Authentication) {
            jwt(AAD) {
                skipWhen { true }
            }
        }
        return
    }

    val jwksUri = try {
        fetchJwksUri(effectiveWellKnownUrl)
    } catch (e: Exception) {
        authLog.error("Failed to fetch JWKS URI from $effectiveWellKnownUrl", e)
        throw e
    }

    val jwkProvider = JwkProviderBuilder(URI(jwksUri).toURL())
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    install(Authentication) {
        jwt(AAD) {
            realm = "aap-fss-proxy"
            verifier(jwkProvider) { 
                acceptLeeway(3)
            }
            validate { credential ->
                val audience = credential.payload.audience
                if (azureConfig.clientId in audience) {
                    JWTPrincipal(credential.payload)
                } else {
                    authLog.warn("Invalid audience in token: $audience, expected: ${azureConfig.clientId}")
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Token is not valid or has expired"))
            }
        }
    }
}

private fun fetchJwksUri(wellKnownUrl: String): String {
    val url = URI(wellKnownUrl).toURL()
    val response = url.readText()
    val regex = """"jwks_uri"\s*:\s*"([^"]+)"""".toRegex()
    return regex.find(response)?.groupValues?.get(1)
        ?: throw IllegalStateException("Could not find jwks_uri in well-known configuration")
}
