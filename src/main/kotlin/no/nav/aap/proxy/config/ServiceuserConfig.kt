package no.nav.aap.proxy.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*
import java.util.Base64.*

@ConfigurationProperties("serviceuser")
data class ServiceuserConfig (val username: String, val password: String) {
    val credentials = getEncoder().encodeToString("$username:$password".toByteArray(UTF_8))
}