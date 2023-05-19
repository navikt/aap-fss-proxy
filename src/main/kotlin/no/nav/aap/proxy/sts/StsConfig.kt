package no.nav.aap.proxy.sts

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.util.Constants.STS

@ConfigurationProperties(STS)
class StsConfig(baseUri : URI,
                val tokenPath : String = "rest/v1/sts/token" ,
                pingPath : String = "",
                enabled : Boolean = true) : AbstractRestConfig(baseUri, pingPath, STS, enabled, RetryConfig.DEFAULT)