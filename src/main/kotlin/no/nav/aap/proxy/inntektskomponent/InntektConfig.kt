package no.nav.aap.proxy.inntektskomponent

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import no.nav.aap.util.Constants.INNTEKTSKOMPONENT

@ConfigurationProperties(INNTEKTSKOMPONENT)
class InntektConfig(baseUri : URI,
                    val path : String = "api/v1/hentinntektliste",
                    pingPath : String = "api/ping",
                    enabled : Boolean = true) : AbstractRestConfig(baseUri, pingPath, INNTEKTSKOMPONENT, enabled, DEFAULT)