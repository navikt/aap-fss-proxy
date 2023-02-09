package no.nav.aap.proxy.inntektskomponent

import java.io.IOException
import java.net.URI
import java.time.Duration
import java.util.function.Predicate
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion
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
                    retry: RetryConfig = RetryConfig.DEFAULT,
                    @DefaultValue("true") enabled: Boolean): AbstractRestConfig(baseUri,pingPath, INNTEKTSKOMPONENT,enabled,retry) {

}