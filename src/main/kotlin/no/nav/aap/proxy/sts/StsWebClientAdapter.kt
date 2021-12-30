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
                .onStatus({ obj: HttpStatus -> obj.isError }) { obj: ClientResponse -> obj.createException() }
                .bodyToMono<OidcToken>()
                .block()
        }
        return token!!.token.tokenAsString
    }

    override fun ping() {
        oidcToken()
    }

    private fun OidcToken?.shouldBeRenewed() = this?.hasExpired() ?: true
}