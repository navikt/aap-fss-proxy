package no.nav.aap.proxy.inntektskomponent

import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.aap.proxy.config.AppConfig

fun Route.inntektRoutes(config: AppConfig) {
    val inntektClient = InntektClient(config)
    
    route("/inntektskomponent") {
        post("/") {
            val request = call.receive<InntektRequest>()
            val response = inntektClient.getInntekt(request)
            call.respond(response)
        }
    }
}
