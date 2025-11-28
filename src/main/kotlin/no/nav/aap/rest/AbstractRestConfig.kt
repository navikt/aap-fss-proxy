package no.nav.aap.rest

import java.io.IOException
import java.net.URI
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeoutException
import java.util.function.Predicate
import org.apache.commons.lang3.exception.ExceptionUtils.hasCause
import org.slf4j.Logger
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.boot.convert.DurationStyle.*
import reactor.util.retry.Retry.*
import no.nav.aap.api.felles.error.RecoverableIntegrationException
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import no.nav.aap.util.Metrikker.inc
import no.nav.aap.util.URIUtil.uri

abstract class AbstractRestConfig(val baseUri : URI, val pingPath : String, name : String = baseUri.host, isEnabled : Boolean,
                                  val retry : RetryConfig = DEFAULT) : AbstractConfig(name, isEnabled) {

    val pingEndpoint = uri(baseUri, pingPath)

    data class RetryConfig(
        @DefaultValue(DEFAULT_RETRIES) val retries : Long,
        @DefaultValue(DEFAULT_DELAY) val delayed : Duration) {

        companion object {

            private const val DEFAULT_RETRIES = "3"
            private const val DEFAULT_DELAY = "1000ms"
            val DEFAULT = RetryConfig(DEFAULT_RETRIES.toLong(), detectAndParse(DEFAULT_DELAY))
        }
    }

    fun retrySpec(log : Logger, path : String = "/", exceptionsFilter : Predicate<in Throwable> = DEFAULT_EXCEPTIONS_PREDICATE) =
        fixedDelay(retry.retries, retry.delayed)
            .filter(exceptionsFilter)
            .onRetryExhaustedThrow { _, s ->
                s.failure().also {
                    inc(METRIKKNAVN, BASE, "$baseUri", PATH, path, EXCEPTION, s.name(), TYPE, EXHAUSTED)
                    log.warn("Retry mot $baseUri/$path gir opp pga. exception ${s.name()} etter ${s.totalRetries()} forsøk")
                }
            }
            .doBeforeRetry {
                log.warn("${it.totalRetries() + 1}. retry mot $baseUri$/$path pga. exception ${it.name()} og melding ${it.failure().message}")
            }
            .doAfterRetry {
                if (it.failure() == null) {
                    log.info("Retry mot $baseUri/$path var vellykket på forsøk  ${it.totalRetries() + 1}")
                    inc(METRIKKNAVN, BASE, "$baseUri", PATH, path, TYPE, SUCCESS)
                }
                else {
                    log.warn("${it.totalRetries() + 1}. retry mot $baseUri/$path feilet på forsøk ${it.totalRetries() + 1} med exception ${it.name()}",it)
                }
            }

    private fun RetrySignal.name() = failure().javaClass.simpleName

    companion object {

        private const val METRIKKNAVN = "webclient"
        private const val BASE = "base"
        private const val PATH = "path"
        private const val EXCEPTION = "exception"
        private const val TYPE = "type"
        private const val EXHAUSTED = "exhausted"
        private const val SUCCESS = "success"
        private val DEFAULT_EXCEPTIONS_PREDICATE = Predicate<Throwable> { hasCause(it, IOException::class.java) || it is RecoverableIntegrationException || it is TimeoutException}
    }

    override fun toString() = "name=$name, pingPath=$pingPath,enabled=$isEnabled,baseUri=$baseUri"
}

abstract class AbstractConfig(val name : String, val isEnabled : Boolean)