package no.nav.aap.sts

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "sts")
class StsConfig @ConstructorBinding constructor(
    val url: String
)