package no.nav.aap.util

import org.slf4j.MDC

object MDCUtil {

    const val NAV_CONSUMER_ID = "Nav-Consumer-Id"
    const val NAV_CONSUMER_ID2 = "consumerId"
    const val NAV_CALL_ID = "Nav-CallId"
    const val NAV_CALL_ID1 = "Nav-Call-Id"
    const val NAV_CALL_ID2 = "callId"
    const val NAV_CALL_ID3 = "X-Correlation-ID"

    fun callId() = MDC.get(NAV_CALL_ID) ?: run {
        val id = CallIdGenerator.create()
        toMDC(NAV_CALL_ID, id)
        id
    }

    fun consumerId(defaultValue: String): String = MDC.get(NAV_CONSUMER_ID) ?: run {
        toMDC(NAV_CONSUMER_ID, defaultValue)
        defaultValue
    }

    fun toMDC(key: String, value: String?, defaultValue: String? = null) =
        MDC.put(key, value ?: defaultValue)
}