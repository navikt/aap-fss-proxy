package no.nav.aap.joark

import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URI

@ConfigurationProperties("joark")
class JoarkConfig @ConstructorBinding constructor(baseUri: URI,path: String,@DefaultValue("true") enabled: Boolean): AbstractRestConfig(baseUri,path,enabled)