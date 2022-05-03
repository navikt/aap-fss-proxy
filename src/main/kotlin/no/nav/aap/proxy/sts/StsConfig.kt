package no.nav.aap.proxy.sts

import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.util.Constants.STS
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URI

@ConfigurationProperties(STS)
@ConstructorBinding
class StsConfig (baseUri: URI,
                 @DefaultValue("rest/v1/sts/token") val tokenPath: String,
                 @DefaultValue("") pingPath: String,
                 @DefaultValue("true") enabled: Boolean): AbstractRestConfig(baseUri,pingPath,enabled)