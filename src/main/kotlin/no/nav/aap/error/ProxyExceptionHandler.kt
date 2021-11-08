package no.nav.aap.error

import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.WebClientResponseException.*
import org.zalando.problem.Problem
import org.zalando.problem.Status
import org.zalando.problem.Status.*
import org.zalando.problem.spring.web.advice.ProblemHandling

@ControllerAdvice
class ProxyExceptionHandler: ProblemHandling {

    @ExceptionHandler(JwtTokenUnauthorizedException::class, JwtTokenMissingException::class)
    fun handleMissingOrExpiredToken(e: java.lang.Exception, req: NativeWebRequest): ResponseEntity<Problem> {
        return create(UNAUTHORIZED,e,req)
    }
    @ExceptionHandler(IntegrationException::class)
    fun handleIntegrationException(e: java.lang.Exception, req: NativeWebRequest): ResponseEntity<Problem> {
        return create(UNPROCESSABLE_ENTITY,e,req)
    }
    @ExceptionHandler(WebClientResponseException::class)
    fun handleWebClientResponseException(e: WebClientResponseException, req: NativeWebRequest): ResponseEntity<Problem> {
        return when (e) {
            is Forbidden, is Unauthorized -> create(UNAUTHORIZED,e,req)
            is NotFound -> create(NOT_FOUND,e,req)
            else -> create(INTERNAL_SERVER_ERROR,e,req)
        }
    }
}