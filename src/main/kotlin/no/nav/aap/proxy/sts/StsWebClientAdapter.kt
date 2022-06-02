package no.nav.aap.proxy.sts

import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.Constants.STS
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Component
class StsWebClientAdapter (@Qualifier(STS) webClient: WebClient, private val cf: StsConfig) : AbstractWebClientAdapter(webClient, cf) {
    //private var token: OidcToken? = null

    fun oidcToken(): String {
       // if (token.shouldBeRenewed()) {
            val token = webClient.get()
                .uri { b -> b.path(cf.tokenPath)
                    .queryParam("grant_type", "client_credentials")
                    .queryParam("scope", "openid")
                    .build()
                }
                .retrieve()
                .bodyToMono<OidcToken>()
                .doOnError { t: Throwable -> log.warn("STS oppslag feilet", t) }
                .doOnSuccess { log.trace("STS oppslag OK, utgår om ${it.expiresIn}s") }
                .block()
        //}
        val date = convertToLocalDateViaInstant(token?.accessToken?.jwtTokenClaims?.get("exp") as Date)
        log.info("Token expiry at $date")
        log.info("Expires in ${token?.expiresIn}")
        return token!!.accessToken!!.tokenAsString
    }

    fun convertToLocalDateViaInstant(d: Date): LocalDateTime {
        return d.toInstant()
            .atZone(ZoneId.of("GMT+1"))
            .toLocalDateTime()
    }
    override fun ping() {
        oidcToken()
    }

    private fun OidcToken?.shouldBeRenewed() = this?.hasExpired() ?: true
}