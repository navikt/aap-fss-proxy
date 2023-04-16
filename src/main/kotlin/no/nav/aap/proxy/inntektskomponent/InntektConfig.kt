package no.nav.aap.proxy.inntektskomponent

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import no.nav.aap.util.Constants.INNTEKTSKOMPONENT

@ConfigurationProperties(INNTEKTSKOMPONENT)
class InntektConfig(baseUri : URI,
                    @DefaultValue("api/v1/hentinntektliste") val path : String,
                    @DefaultValue("api/ping") pingPath : String,
                    @DefaultValue("true") enabled : Boolean) : AbstractRestConfig(baseUri, pingPath, INNTEKTSKOMPONENT, enabled, DEFAULT)