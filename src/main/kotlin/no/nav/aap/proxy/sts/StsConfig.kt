package no.nav.aap.proxy.sts

import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URI

@ConfigurationProperties("sts")
class StsConfig @ConstructorBinding constructor(baseUri: URI,
                                                @DefaultValue("rest/v1/sts/token") val tokenPath: String,
                                                @DefaultValue("") pingPath: String,
                                                @DefaultValue("true") enabled: Boolean): AbstractRestConfig(baseUri,pingPath,enabled)