package no.nav.aap.sts

import no.nav.aap.rest.RetryAware
import no.nav.aap.sts.domain.OidcToken
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class StsClient(
    private val stsWebClient: WebClient
): RetryAware {
    private var cachedOidcToken: OidcToken? = null

    fun oidcToken(): String {
        if (cachedOidcToken.shouldBeRenewed()) {
            runCatching {
                cachedOidcToken = stsWebClient.get()
                    .uri { uriBuilder ->
                        uriBuilder
                            .queryParam("grant_type", "client_credentials")
                            .queryParam("scope", "openid")
                            .build()
                    }
                    .retrieve()
                    .bodyToMono<OidcToken>()
                    .block()
            }.onFailure {
                throw RuntimeException("STS er utilgjengelig")
            }
        }

        return cachedOidcToken!!.token
    }

    private fun OidcToken?.shouldBeRenewed(): Boolean = this?.hasExpired() ?: true
}
