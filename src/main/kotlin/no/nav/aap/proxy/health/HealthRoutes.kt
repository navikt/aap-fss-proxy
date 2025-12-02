package no.nav.aap.proxy.health

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.aap.proxy.plugins.prometheusMeterRegistry

fun Route.healthRoutes() {
    route("/internal") {
        get("/isalive") {
            call.respondText("ALIVE", ContentType.Text.Plain)
        }
        
        get("/isready") {
            call.respondText("READY", ContentType.Text.Plain)
        }
        
        get("/prometheus") {
            call.respondText(prometheusMeterRegistry.scrape(), ContentType.Text.Plain)
        }
        
        get("/metrics") {
            call.respondText(prometheusMeterRegistry.scrape(), ContentType.Text.Plain)
        }
    }
}
