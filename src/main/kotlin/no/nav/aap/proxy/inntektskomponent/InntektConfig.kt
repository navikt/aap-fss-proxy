package no.nav.aap.proxy.inntektskomponent

import java.io.IOException
import java.net.URI
import java.time.Duration
import java.util.function.Predicate
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.util.Constants.INNTEKTSKOMPONENT
import org.apache.commons.lang3.exception.ExceptionUtils.hasCause
import org.slf4j.Logger
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.boot.convert.DurationStyle.detectAndParse
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.WebClientResponseException.Forbidden
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import org.springframework.web.reactive.function.client.WebClientResponseException.Unauthorized
import reactor.util.retry.Retry.fixedDelay

@ConfigurationProperties(INNTEKTSKOMPONENT)
class InntektConfig(baseUri: URI,
                    @DefaultValue("api/v1/hentinntektliste") val path: String,
                    @DefaultValue("api/ping") pingPath: String,
                    @NestedConfigurationProperty private val retry: RetryConfig = RetryConfig.DEFAULT,
                    @DefaultValue("true") enabled: Boolean): AbstractRestConfig(baseUri,pingPath, INNTEKTSKOMPONENT,enabled) {


    fun retrySpec(log: Logger, exceptionsFilter: Predicate<in Throwable> = DEFAULT_EXCEPTIONS_PREDICATE) =
        fixedDelay(retry.retries, retry.delayed)
            .filter(exceptionsFilter)
            .onRetryExhaustedThrow { _, s -> s.failure().also { log.warn("Retry kall mot  $baseUri gir opp med  ${s.failure().javaClass.simpleName} etter ${s.totalRetries()} forsøk") } }
            .doAfterRetry  { s -> log.info("Retry kall mot $baseUri grunnet exception ${s.failure().javaClass.simpleName} og melding ${s.failure().message} gjort for ${s.totalRetriesInARow() + 1} gang") }
            .doBeforeRetry { s -> log.info("Retry kall mot $baseUri grunnet exception ${s.failure().javaClass.simpleName} og melding ${s.failure().message} for ${s.totalRetriesInARow() + 1} gang, prøver igjen") }


    companion object  {
        private val DEFAULT_EXCEPTIONS_PREDICATE = Predicate<Throwable> { hasCause(it, IOException::class.java) || (it is WebClientResponseException && it !is Unauthorized && it !is NotFound && it !is Forbidden) }
    }

    data class RetryConfig(@DefaultValue(DEFAULT_RETRIES)  val retries: Long,
                           @DefaultValue(DEFAULT_DELAY)  val delayed: Duration) {
        companion object {
            const val DEFAULT_RETRIES = "3"
            const val DEFAULT_DELAY = "100ms"
            val DEFAULT = RetryConfig(DEFAULT_RETRIES.toLong(), detectAndParse(DEFAULT_DELAY))
        }
    }

}