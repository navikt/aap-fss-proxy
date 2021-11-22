package no.nav.aap.util

import org.slf4j.MDC
import java.util.*

object MDCUtil {
    const val NAV_CONSUMER_ID = "Nav-Consumer-Id"
    const val NAV_CONSUMER_ID2 = "consumerId"
    const val NAV_CALL_ID = "Nav-CallId"
    const val NAV_CALL_ID1 = "Nav-Call-Id"
    const val NAV_CALL_ID2 = "callId"
    fun callId(): String {
        return MDC.get(NAV_CALL_ID) ?: UUID.randomUUID().toString()
    }

    fun consumerId() = MDC.get(NAV_CONSUMER_ID) ?: "aap-fss-proxy"

    fun toMDC(key: String, value: String?, defaultValue: String? = null) {
        MDC.put(key, value ?: defaultValue)
    }
}