package no.nav.aap.rest

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.stereotype.Component


@Component
class HeadersToMDCFilterRegistrationBean(headersFilter: HeadersToMDCFilterBean?) : FilterRegistrationBean<HeadersToMDCFilterBean?>() {
    init {
        filter = headersFilter
        urlPatterns =listOf(ALWAYS)
    }
    companion object {
        private const val ALWAYS = "/*"
    }
}