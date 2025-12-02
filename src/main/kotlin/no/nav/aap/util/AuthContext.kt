package no.nav.aap.util

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.ThreadContextElement
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import no.nav.security.token.support.core.context.TokenValidationContextHolder

class AuthContext(private val ctxHolder : TokenValidationContextHolder) {

    override fun toString() = "${javaClass.simpleName} [ctxHolder=$ctxHolder]"
}

class RequestContextCoroutineContext(private val requestAttributes: RequestAttributes? = RequestContextHolder.getRequestAttributes()) : ThreadContextElement<RequestAttributes?> {
    companion object Key : CoroutineContext.Key<RequestContextCoroutineContext>

    override val key: CoroutineContext.Key<RequestContextCoroutineContext> get() = Key

    override fun updateThreadContext(context: CoroutineContext): RequestAttributes? {
        val previousAttributes =  RequestContextHolder.getRequestAttributes()
        RequestContextHolder.setRequestAttributes(requestAttributes)
        return previousAttributes
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: RequestAttributes?) {
        oldState?.let {
            RequestContextHolder.setRequestAttributes(oldState)
        } ?: RequestContextHolder.resetRequestAttributes()
    }
}
