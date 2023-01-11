package no.nav.aap.proxy.config

import java.nio.charset.StandardCharsets.UTF_8
import java.util.*
import java.util.Base64.*
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("serviceuser")
data class ServiceuserConfig (val username: String, val password: String) {
    val credentials = getEncoder().encodeToString("$username:$password".toByteArray(UTF_8))
}

