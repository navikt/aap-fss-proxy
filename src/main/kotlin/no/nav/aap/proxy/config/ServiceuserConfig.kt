package no.nav.aap.proxy.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "serviceuser")
@ConstructorBinding
class ServiceuserConfig (val username: String, val password: String)