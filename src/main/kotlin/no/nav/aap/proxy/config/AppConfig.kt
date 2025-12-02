package no.nav.aap.proxy.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource

data class AppConfig(
    val server: ServerConfig = ServerConfig(),
    val arena: ArenaConfig = ArenaConfig(),
    val arenasoap: ArenaSoapConfig = ArenaSoapConfig(),
    val inntektskomponent: InntektConfig = InntektConfig(),
    val sts: StsConfig = StsConfig(),
    val serviceuser: ServiceuserConfig = ServiceuserConfig(),
    val azure: AzureConfig = AzureConfig()
) {
    companion object {
        fun load(): AppConfig {
            val profile = System.getenv("NAIS_CLUSTER_NAME")?.let {
                when {
                    it.contains("dev") -> "dev-fss"
                    it.contains("prod") -> "prod-fss"
                    else -> null
                }
            }
            
            val builder = ConfigLoaderBuilder.default()
                .addResourceSource("/application.yaml")
            
            if (profile != null) {
                builder.addResourceSource("/application-$profile.yaml", optional = true)
            }
            
            return builder.build().loadConfigOrThrow<AppConfig>()
        }
    }
}

data class ServerConfig(
    val port: Int = 8080
)

data class ArenaConfig(
    val baseUri: String = "http://localhost/",
    val path: String = "/v1/aap/sisteVedtak",
    val tokenPath: String = "/oauth/token",
    val pingPath: String = "v1/test/ping",
    val credentials: ArenaCredentials = ArenaCredentials(),
    val enabled: Boolean = true
)

data class ArenaCredentials(
    val id: String = "",
    val secret: String = ""
)

data class ArenaSoapConfig(
    val baseUri: String = "http://localhost/",
    val oppgaveUri: String = "http://localhost/",
    val behandleSakOgAktivitetUri: String = "http://localhost/",
    val sts: ArenaStsConfig = ArenaStsConfig(),
    val credentials: ArenaCredentials = ArenaCredentials(),
    val saker: String = "/ArenaSakVedtakService",
    val enabled: Boolean = true
) {
    val sakerUri: String get() = "$baseUri$saker"
}

data class ArenaStsConfig(
    val url: String = "http://localhost/",
    val username: String = "",
    val password: String = ""
)

data class InntektConfig(
    val baseUri: String = "http://localhost/",
    val path: String = "api/v1/hentinntektliste",
    val pingPath: String = "api/ping",
    val enabled: Boolean = true
)

data class StsConfig(
    val baseUri: String = "http://localhost/",
    val tokenPath: String = "rest/v1/sts/token",
    val pingPath: String = "",
    val enabled: Boolean = true
)

data class ServiceuserConfig(
    val username: String = "",
    val password: String = ""
) {
    val credentials: String by lazy {
        java.util.Base64.getEncoder().encodeToString("$username:$password".toByteArray())
    }
}

data class AzureConfig(
    val app: AzureAppConfig = AzureAppConfig()
)

data class AzureAppConfig(
    val clientId: String = System.getenv("AZURE_APP_CLIENT_ID") ?: "",
    val tenantId: String = System.getenv("AZURE_APP_TENANT_ID") ?: "",
    val wellKnownUrl: String = System.getenv("AZURE_APP_WELL_KNOWN_URL") ?: ""
) {
    // Only construct a default URL if tenantId is present, otherwise leave empty
    val effectiveWellKnownUrl: String get() = wellKnownUrl.ifEmpty {
        if (tenantId.isNotEmpty()) {
            "https://login.microsoftonline.com/$tenantId/v2.0/.well-known/openid-configuration"
        } else {
            ""
        }
    }
}
