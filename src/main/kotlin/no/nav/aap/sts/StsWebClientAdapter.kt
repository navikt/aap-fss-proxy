package no.nav.aap.sts

import no.nav.aap.config.Constants
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class StsWebClientAdapter internal constructor(@Qualifier(Constants.STS) webClient: WebClient, cfg: StsConfig) : AbstractWebClientAdapter(webClient, cfg) {
    private var cachedOidcToken: OidcToken? = null

    fun oidcToken(): String {
        if (cachedOidcToken.shouldBeRenewed()) {
            cachedOidcToken = webClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .queryParam("grant_type", "client_credentials")
                        .queryParam("scope", "openid")
                        .build()
                }
                .retrieve()
                .onStatus({ obj: HttpStatus -> obj.isError }) { obj: ClientResponse -> obj.createException() }
                .bodyToMono<OidcToken>()
                .block()
        }
        return cachedOidcToken!!.token.tokenAsString
    }

    override fun ping() {
        oidcToken()
    }

    private fun OidcToken?.shouldBeRenewed(): Boolean = this?.hasExpired() ?: true
}