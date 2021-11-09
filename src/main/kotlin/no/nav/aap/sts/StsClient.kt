package no.nav.aap.sts

import no.nav.aap.rest.RetryAware
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class StsClient(private val stsWebClient: WebClient): RetryAware {
    private var cachedOidcToken: OidcToken? = null

    fun oidcToken(): String {
        if (cachedOidcToken.shouldBeRenewed()) {
                cachedOidcToken = stsWebClient.get()
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
    private fun OidcToken?.shouldBeRenewed(): Boolean = this?.hasExpired() ?: true
}