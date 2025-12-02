package no.nav.aap.proxy.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

private val statusLog = LoggerFactory.getLogger("StatusPages")

data class ProblemDetail(
    val type: String = "about:blank",
    val title: String,
    val status: Int,
    val detail: String?,
    val instance: String? = null,
    val navCallId: String? = null
)

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            statusLog.warn("Bad request: ${cause.message}", cause)
            call.respond(
                HttpStatusCode.BadRequest,
                ProblemDetail(
                    title = "Bad Request",
                    status = 400,
                    detail = cause.message,
                    navCallId = CallIdUtil.callId()
                )
            )
        }
        
        exception<SecurityException> { call, cause ->
            statusLog.warn("Unauthorized: ${cause.message}", cause)
            call.respond(
                HttpStatusCode.Unauthorized,
                ProblemDetail(
                    title = "Unauthorized",
                    status = 401,
                    detail = cause.message,
                    navCallId = CallIdUtil.callId()
                )
            )
        }
        
        exception<NoSuchElementException> { call, cause ->
            statusLog.warn("Not found: ${cause.message}", cause)
            call.respond(
                HttpStatusCode.NotFound,
                ProblemDetail(
                    title = "Not Found",
                    status = 404,
                    detail = cause.message,
                    navCallId = CallIdUtil.callId()
                )
            )
        }
        
        exception<IntegrationException> { call, cause ->
            statusLog.error("Integration error: ${cause.message}", cause)
            val status = when (cause) {
                is IrrecoverableIntegrationException -> HttpStatusCode.InternalServerError
                is RecoverableIntegrationException -> HttpStatusCode.ServiceUnavailable
                else -> HttpStatusCode.InternalServerError
            }
            call.respond(
                status,
                ProblemDetail(
                    title = status.description,
                    status = status.value,
                    detail = cause.message,
                    navCallId = CallIdUtil.callId()
                )
            )
        }
        
        exception<Throwable> { call, cause ->
            statusLog.error("Unexpected error: ${cause.message}", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ProblemDetail(
                    title = "Internal Server Error",
                    status = 500,
                    detail = cause.message ?: "An unexpected error occurred",
                    navCallId = CallIdUtil.callId()
                )
            )
        }
    }
}

abstract class IntegrationException(message: String?, cause: Throwable? = null) : RuntimeException(message, cause)
open class RecoverableIntegrationException(message: String?, cause: Throwable? = null) : IntegrationException(message, cause)
open class IrrecoverableIntegrationException(message: String?, cause: Throwable? = null) : IntegrationException(message, cause)
