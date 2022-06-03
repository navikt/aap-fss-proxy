package no.nav.aap.proxy.sts

import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.Constants.STS
import no.nav.aap.util.LoggerUtil
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class StsWebClientAdapter(@Qualifier(STS) webClient: WebClient, private val cf: StsConfig) :
    AbstractWebClientAdapter(webClient, cf) {

    var token  = getTheToken()

    fun oidcToken(): OidcToken {
        if (token.shouldBeRenewed()) {
            log.trace("Fornyer token")
            token = getTheToken()
        }
        return token
    }

    private fun getTheToken() =
         webClient.get()
            .uri { b ->
                b.path(cf.tokenPath)
                    .queryParam("grant_type", "client_credentials")
                    .queryParam("scope", "openid")
                    .build()
            }
            .retrieve()
            .bodyToMono<OidcToken>()
            .doOnError { t: Throwable -> log.warn("STS oppslag feilet", t) }
            .doOnSuccess { log.trace("STS oppslag OK, utgår om ${it.expiresIn}s") }
            .block() ?: throw IllegalStateException("Ingen respons fra STS")

    override fun ping() {
       oidcToken()
    }

    private fun OidcToken?.shouldBeRenewed() = this?.hasExpired() ?: true
}