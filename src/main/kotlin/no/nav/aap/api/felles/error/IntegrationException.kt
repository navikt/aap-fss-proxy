package no.nav.aap.api.felles.error

import java.net.URI
import org.springframework.http.HttpStatus

abstract class IntegrationException(msg : String?, uri : URI? = null, cause : Throwable? = null) : RuntimeException(msg, cause)

open class RecoverableIntegrationException(msg : String?, uri : URI? = null, cause : Throwable? = null) : IntegrationException(msg, uri, cause)

open class IrrecoverableIntegrationException(msg : String?, uri : URI? = null, cause : Throwable? = null) : IntegrationException(msg, uri, cause)

abstract class IrrecoverableGraphQLException(status : HttpStatus, msg : String,cause : Throwable? = null) : IrrecoverableIntegrationException("$msg (${status.value()})",
    null, cause) {

    class NotFoundGraphQLException(status : HttpStatus, msg : String) : IrrecoverableGraphQLException(status, msg)
    class BadGraphQLException(status : HttpStatus, msg : String,cause : Throwable? = null) : IrrecoverableGraphQLException(status, msg,cause)
    class UnauthenticatedGraphQLException(status : HttpStatus, msg : String) : IrrecoverableGraphQLException(status, msg)
    class UnauthorizedGraphQLException(status : HttpStatus, msg : String) : IrrecoverableGraphQLException(status, msg)
}

abstract class RecoverableGraphQLException(status : HttpStatus, msg : String, cause : Throwable?)
    : RecoverableIntegrationException("${status.value()}-$msg", cause = cause) {

    class UnhandledGraphQLException(status : HttpStatus, msg : String, cause : Throwable? = null) : RecoverableGraphQLException(status, msg, cause)
}