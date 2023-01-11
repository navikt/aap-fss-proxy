package no.nav.aap.proxy.rest

import java.io.IOException
import java.time.Duration
import java.util.function.Predicate
import no.nav.aap.rest.AbstractRestConfig
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.WebClientResponseException.Forbidden
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import org.springframework.web.reactive.function.client.WebClientResponseException.Unauthorized
import reactor.util.retry.Retry

object AbstractRestConfigExtensions {

    fun AbstractRestConfig.retrySpec(log: Logger, exceptionsFilter: Predicate<in Throwable> = DEFAULT_EXCEPTIONS_PREDICATE) =
        Retry.fixedDelay(3, Duration.ofMillis(100))
            .filter(exceptionsFilter)
            .onRetryExhaustedThrow { _, s -> s.failure().also { log.warn("Retry kall mot  $baseUri gir opp med  ${s.failure().javaClass.simpleName} etter ${s.totalRetries()} forsøk") } }
            .doAfterRetry  { s -> log.info("Retry kall mot $baseUri grunnet exception ${s.failure().javaClass.simpleName} og melding ${s.failure().message} gjort for ${s.totalRetriesInARow() + 1} gang") }
            .doBeforeRetry { s -> log.info("Retry kall mot $baseUri grunnet exception ${s.failure().javaClass.simpleName} og melding ${s.failure().message} for ${s.totalRetriesInARow() + 1} gang, prøver igjen") }


        private val DEFAULT_EXCEPTIONS_PREDICATE = Predicate<Throwable> { ExceptionUtils.hasCause(it,
                IOException::class.java) || (it is WebClientResponseException && it !is Unauthorized && it !is NotFound && it !is Forbidden) }
}