package no.nav.aap.proxy.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.MDC
import org.slf4j.event.Level
import java.util.*

const val NAV_CALL_ID = "Nav-CallId"
const val NAV_CONSUMER_ID = "Nav-Consumer-Id"

fun Application.configureCallLogging() {
    install(CallId) {
        header(NAV_CALL_ID)
        generate { UUID.randomUUID().toString() }
        verify { it.isNotEmpty() }
    }
    
    install(CallLogging) {
        level = Level.INFO
        callIdMdc(NAV_CALL_ID)
        
        filter { call -> 
            !call.request.path().contains("/internal/") 
        }
        
        format { call ->
            val status = call.response.status()
            val method = call.request.httpMethod.value
            val path = call.request.path()
            "Status: $status, Method: $method, Path: $path"
        }
        
        mdc(NAV_CONSUMER_ID) { call ->
            call.request.header(NAV_CONSUMER_ID) ?: "unknown"
        }
    }
}

object CallIdUtil {
    fun callId(): String = MDC.get(NAV_CALL_ID) ?: UUID.randomUUID().toString()
    fun consumerId(default: String): String = MDC.get(NAV_CONSUMER_ID) ?: default
}
