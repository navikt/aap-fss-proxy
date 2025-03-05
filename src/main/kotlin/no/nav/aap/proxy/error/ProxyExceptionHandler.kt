package no.nav.aap.proxy.error

import jakarta.xml.ws.soap.SOAPFaultException
import no.nav.aap.proxy.arena.generated.oppgave.BestillOppgavePersonErInaktiv
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.*
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.WebClientResponseException.*
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import no.nav.aap.proxy.arena.generated.oppgave.BestillOppgavePersonIkkeFunnet
import no.nav.aap.proxy.arena.generated.oppgave.BestillOppgaveSikkerhetsbegrensning
import no.nav.aap.proxy.arena.generated.oppgave.BestillOppgaveUgyldigInput
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.callId
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException

@ControllerAdvice
class ProxyExceptionHandler : ResponseEntityExceptionHandler() {

    private val log = getLogger(javaClass)

    @ExceptionHandler(JwtTokenUnauthorizedException::class, JwtTokenMissingException::class)
    fun handleMissingOrExpiredToken(e : Exception, req : NativeWebRequest) = create(UNAUTHORIZED, e, req)

    @ExceptionHandler(WebClientResponseException::class)
    fun handleWebClientResponseException(e : WebClientResponseException, req : NativeWebRequest) =
        when (e) {
            is BadRequest -> create(BAD_REQUEST, e, req)
            is Forbidden, is Unauthorized -> create(UNAUTHORIZED, e, req)
            is NotFound -> create(NOT_FOUND, e, req)
            else -> create(INTERNAL_SERVER_ERROR, e, req)
        }

    @ExceptionHandler(SOAPFaultException::class)
    fun soapFault(e : SOAPFaultException, req : NativeWebRequest) = create(INTERNAL_SERVER_ERROR, e, req)

    @ExceptionHandler(Exception::class)
    fun catchAll(e : Exception, req : NativeWebRequest) = create(INTERNAL_SERVER_ERROR, e, req)

    @ExceptionHandler(BestillOppgavePersonIkkeFunnet::class, BestillOppgavePersonIkkeFunnet::class)
    fun ikkeFunnet(e : Exception, req : NativeWebRequest) = create(NOT_FOUND, e, req)

    @ExceptionHandler(BestillOppgaveSikkerhetsbegrensning::class)
    fun sikkerhet(e : BestillOppgaveSikkerhetsbegrensning, req : NativeWebRequest) = create(UNAUTHORIZED, e, req)

    @ExceptionHandler(BestillOppgaveUgyldigInput::class)
    fun ugyldigInput(e : BestillOppgaveUgyldigInput, req : NativeWebRequest) = create(BAD_REQUEST, e, req)

    private fun create(status : HttpStatus, e : Exception, req : NativeWebRequest) =
        ResponseEntity.status(status)
            .headers(HttpHeaders().apply { contentType = APPLICATION_PROBLEM_JSON })
            .body(createProblemDetail(e, status, e.message ?: e.javaClass.simpleName, null, null, req).apply {
                setProperty(NAV_CALL_ID, callId())
            }.also { log(e, it, req, status) })

    private fun log(t : Throwable, problem : ProblemDetail, req : NativeWebRequest, status : HttpStatus) {
        when (t.cause) {
            BestillOppgavePersonIkkeFunnet::class, BestillOppgavePersonErInaktiv::class -> log.info("$req $problem ${status.reasonPhrase}: ${t.message}", t)
            else -> log.error("$req $problem ${status.reasonPhrase}: ${t.message}", t)
        }
    }
}