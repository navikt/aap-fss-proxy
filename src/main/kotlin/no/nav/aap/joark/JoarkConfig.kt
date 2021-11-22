package no.nav.aap.joark

import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URI

@ConfigurationProperties(prefix = "joark")
class JoarkConfig @ConstructorBinding constructor(uri: URI,path: String,@DefaultValue("true") enabled: Boolean): AbstractRestConfig(uri,path,enabled)