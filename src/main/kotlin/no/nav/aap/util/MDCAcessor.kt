package no.nav.aap.util

import io.micrometer.context.ContextRegistry.getInstance
import io.micrometer.context.ThreadLocalAccessor
import org.slf4j.MDC.clear
import org.slf4j.MDC.getCopyOfContextMap
import org.slf4j.MDC.setContextMap
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder.getRequestAttributes
import org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes
import org.springframework.web.context.request.RequestContextHolder.setRequestAttributes
import reactor.core.publisher.Hooks.enableAutomaticContextPropagation

private class MDCAccessor : ThreadLocalAccessor<Map<String, String>> {

    override fun key() = "mdc"

    override fun getValue() = getCopyOfContextMap() ?: emptyMap()

    override fun setValue(value : Map<String, String>) = setContextMap(value)

    override fun reset() = clear()
}

private class RequestAttributesAccessor : ThreadLocalAccessor<RequestAttributes> {

    override fun key() = RequestAttributesAccessor::class.java.name

    override fun getValue() = getRequestAttributes()

    override fun setValue(value : RequestAttributes) = setRequestAttributes(value)

    override fun reset() = resetRequestAttributes()
}

object AccessorUtil {

    fun init() {
        enableAutomaticContextPropagation()
        getInstance().apply {
            registerThreadLocalAccessor(RequestAttributesAccessor())
            registerThreadLocalAccessor(MDCAccessor())
        }
    }
}