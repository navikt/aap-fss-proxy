package no.nav.aap.joark.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "joark")
class JoarkConfig @ConstructorBinding constructor(
    val url: String
)
