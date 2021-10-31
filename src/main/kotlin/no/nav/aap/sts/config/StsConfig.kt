package no.nav.aap.sts.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "sts")
class StsConfig @ConstructorBinding constructor(
    val url: String
)
