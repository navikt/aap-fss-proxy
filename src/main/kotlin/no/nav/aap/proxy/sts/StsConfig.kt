package no.nav.aap.proxy.sts

import java.net.URI
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion
import no.nav.aap.util.Constants.STS
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(STS)
class StsConfig (baseUri: URI,
                 @DefaultValue("rest/v1/sts/token") val tokenPath: String,
                 @DefaultValue("") pingPath: String,
<<<<<<< HEAD
                 @DefaultValue("true") enabled: Boolean): AbstractRestConfig(baseUri,pingPath,STS,enabled,RetryConfig.DEFAULT)
=======
                 @DefaultValue("true") enabled: Boolean): AbstractRestConfig(baseUri,pingPath,STS,enabled,
        RetryConfig.DEFAULT)
>>>>>>> 9f5344f (reflect bump with retry)
