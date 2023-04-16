package no.nav.aap.proxy.sts

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.util.Constants.STS

@ConfigurationProperties(STS)
class StsConfig(baseUri : URI,
                @DefaultValue("rest/v1/sts/token") val tokenPath : String,
                @DefaultValue("") pingPath : String,
                @DefaultValue("true") enabled : Boolean) : AbstractRestConfig(baseUri, pingPath, STS, enabled, RetryConfig.DEFAULT)