package no.nav.aap.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.springframework.boot.SpringBootVersion
import org.springframework.boot.actuate.endpoint.SanitizableData
import org.springframework.boot.actuate.endpoint.SanitizingFunction
import org.springframework.boot.actuate.info.Info.Builder
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.context.ApplicationContext
import org.springframework.core.SpringVersion

class StartupInfoContributor(private val ctx : ApplicationContext) : InfoContributor {

    override fun contribute(builder : Builder) {
        builder.withDetail("extra-info", mapOf("Startup time" to ctx.startupDate.local(),
            "Spring Boot version" to SpringBootVersion.getVersion(),
            "Spring Framework version" to SpringVersion.getVersion()))
    }

    private fun Long.local(fmt : String = "yyyy-MM-dd HH:mm:ss") = LocalDateTime.ofInstant(Instant.ofEpochMilli(this),
        ZoneId.of("Europe/Oslo")).format(DateTimeFormatter.ofPattern(fmt))
}

class PropertyValueSanitzer : SanitizingFunction {

    override fun apply(data : SanitizableData) : SanitizableData {
        with(data) {
            if (key.contains("jwk", ignoreCase = true)) {
                return@with withValue(MASK)
            }
            if (key.contains("private-key", ignoreCase = true)) {
                return@with withValue(MASK)
            }
            if (key.contains("password", ignoreCase = true)) {
                return@with withValue(MASK)
            }
            return@with this
        }
        return data
    }

    companion object {

        private const val MASK = "******"
    }
}