package no.nav.aap.proxy.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.aap.proxy.arena.arenaRoutes
import no.nav.aap.proxy.config.AppConfig
import no.nav.aap.proxy.health.healthRoutes
import no.nav.aap.proxy.inntektskomponent.inntektRoutes

fun Application.configureRouting(config: AppConfig) {
    routing {
        // Internal endpoints (no auth)
        healthRoutes()
        
        // Protected endpoints
        authenticate(AAD) {
            arenaRoutes(config)
            inntektRoutes(config)
        }
    }
}
