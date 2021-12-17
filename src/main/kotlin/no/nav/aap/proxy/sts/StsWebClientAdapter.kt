package no.nav.aap.proxy.sts

import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import no.nav.aap.util.Constants.STS

@Component
class StsWebClientAdapter internal constructor(@Qualifier(STS) webClient: WebClient, private val cf: StsConfig) : AbstractWebClientAdapter(webClient, cf) {
    private var cachedOidcToken: OidcToken? = null

    fun oidcToken(): String {
        if (cachedOidcToken.shouldBeRenewed()) {
            cachedOidcToken = webClient.get()
                .uri { b -> b.path(cf.tokenPath).queryParam("grant_type", "client_credentials").queryParam("scope", "openid").build() }
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