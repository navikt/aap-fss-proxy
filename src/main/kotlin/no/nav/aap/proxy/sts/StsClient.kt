package no.nav.aap.proxy.sts

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.Constants.STS
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import no.nav.boot.conditionals.EnvUtil.isDevOrLocal
import no.nav.security.token.support.core.jwt.JwtToken
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.netty.http.client.HttpClient
import java.net.URI
import java.time.LocalDateTime
import java.util.Base64.getEncoder
import javax.inject.Inject
import kotlin.text.Charsets.UTF_8

@Configuration
class StsClientBeanConfig
    @Component
    class StsClient @Inject constructor(private val a: StsWebClientAdapter)  {
        constructor(webClient: WebClient, cfg: StsConfig) : this(StsWebClientAdapter(webClient,cfg))
        fun oidcToken() = a.oidcToken()
    }

    @Component
    class StsWebClientAdapter (@Qualifier(STS) webClient: WebClient, private val cf: StsConfig) : AbstractWebClientAdapter(webClient, cf) {
        private var token: OidcToken? = null

        fun oidcToken(): String {
            if (token.shouldBeRenewed()) {
                token = webClient.get()
                    .uri { b -> b.path(cf.tokenPath)
                        .queryParam("grant_type", "client_credentials")
                        .queryParam("scope", "openid")
                        .build()
                    }
                    .retrieve()
                    .bodyToMono<OidcToken>()
                    .block()
                    .also { log.trace(CONFIDENTIAL,"STS respons er $it") }
            }
            return token!!.accessToken.tokenAsString
        }

        override fun ping() {
            oidcToken()
        }

        private fun OidcToken?.shouldBeRenewed() = this?.hasExpired() ?: true
    }
    @Bean
    @Qualifier(STS)
    fun stsWebClient(builder: WebClient.Builder, stsCfg: StsConfig, cfg: ServiceuserConfig,env: Environment): WebClient {
        return builder
            .baseUrl("${stsCfg.baseUri}")
            .clientConnector(ReactorClientHttpConnector(HttpClient.create().wiretap(isDevOrLocal(env))))
            .filter(stsExchangeFilterFunction(cfg.basicCredentials()))
            .build()
    }

    @Bean
    fun stsHealthIndicator(a: StsWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}

    private fun stsExchangeFilterFunction(credentials: String) =
        ExchangeFilterFunction { req, next -> next.exchange(ClientRequest.from(req).header(AUTHORIZATION, "$credentials").build()) }


@ConfigurationProperties(prefix = "serviceuser")
@ConstructorBinding
class ServiceuserConfig (val username: String, val password: String) {
    fun basicCredentials() = "Basic " + getEncoder().encodeToString("$username:$password".toByteArray(UTF_8))
}

@ConfigurationProperties("sts")
@ConstructorBinding
class StsConfig(baseUri: URI,
                @DefaultValue("rest/v1/sts/token") val tokenPath: String,
                @DefaultValue("") pingPath: String,
                @DefaultValue("true") enabled: Boolean): AbstractRestConfig(baseUri,pingPath,enabled)

data class OidcToken(@JsonAlias("access_token") val accessToken: JwtToken,
                     @JsonAlias("token_type") val tokenType: String,
                     @JsonAlias("expires_in") val expiresIn: Int) {
    fun hasExpired() = LocalDateTime.now().plusSeconds(expiresIn - 20L).isBefore(LocalDateTime.now())
}