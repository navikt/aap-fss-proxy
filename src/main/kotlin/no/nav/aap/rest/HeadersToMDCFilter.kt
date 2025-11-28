package no.nav.aap.rest

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import java.io.IOException
import no.nav.aap.util.CallIdGenerator
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.NAV_CONSUMER_ID
import no.nav.aap.util.MDCUtil.toMDC

class HeadersToMDCFilter(val applicationName : String) : Filter {

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request : ServletRequest, response : ServletResponse, chain : FilterChain) {
        putValues(HttpServletRequest::class.java.cast(request))
        chain.doFilter(request, response)
    }

    private fun putValues(req : HttpServletRequest) {
        toMDC(NAV_CONSUMER_ID, req.getHeader(NAV_CONSUMER_ID), applicationName)
        toMDC(NAV_CALL_ID, req.getHeader(NAV_CALL_ID), CallIdGenerator.create())
    }

    override fun toString() = javaClass.simpleName + " [ applicationName=" + applicationName + "]"
}