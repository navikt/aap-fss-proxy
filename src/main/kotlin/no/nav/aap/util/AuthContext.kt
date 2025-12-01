package no.nav.aap.util

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.ThreadContextElement
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException

class AuthContext(private val ctxHolder : TokenValidationContextHolder) {

    fun getSubject(issuer : String = IDPORTEN, claim : String = "pid") = getClaim(issuer, claim)
    fun getJti(issuer : String = IDPORTEN) = getClaim(issuer, "jti")
    fun getClaim(issuer : String, claim : String) = claimSet(issuer)?.getStringClaim(claim)
    fun isAuthenticated(issuer : String = IDPORTEN) = getToken(issuer) != null
    private val context get() = ctxHolder.getTokenValidationContext()
    private fun getToken(issuer : String) = context?.getJwtToken(issuer)?.tokenAsString
    private fun claimSet(issuer : String) = context?.getClaims(issuer)
    override fun toString() = "${javaClass.simpleName} [ctxHolder=$ctxHolder]"
    fun getFnr(issuer : String = IDPORTEN, claim : String = "pid") = getSubject(issuer, claim)
        ?.let {
            Fødselsnummer(it)
        } ?: throw JwtTokenMissingException("Intet token i context")
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
