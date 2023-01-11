package no.nav.aap.proxy.arena

import no.nav.aap.proxy.arena.ArenaOIDCConfig.Companion.ARENAOIDC
import no.nav.aap.proxy.rest.AbstractRestConfigExtensions
import no.nav.aap.proxy.rest.AbstractRestConfigExtensions.retrySpec
import no.nav.aap.proxy.sts.OidcToken
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class ArenaOIDCWebClientAdapter(@Qualifier(ARENAOIDC) webClient: WebClient, private val cf: ArenaOIDCConfig) : RenewableOIDCWebClientAdapter(webClient, cf) {
    override fun ping() = mapOf("status" to "OK")  // TODO hvordan pinge denne


}

abstract class RenewableOIDCWebClientAdapter(webClient: WebClient, private val cf: ArenaOIDCConfig) : AbstractWebClientAdapter(webClient, cf) {

    protected var token  = getTheToken()

    fun oidcToken(): String {
        if (token.hasExpired()) {
            log.trace("Fornyer token")
            token = getTheToken()
        }
        return token.accessToken?.tokenAsString!!
    }

    private fun getTheToken() =
        webClient.post()
            .uri { b -> b.path(cf.tokenPath).build() }
            .bodyValue("grant_type=client_credentials")
            .retrieve()
            .bodyToMono<OidcToken>()
            .retryWhen(cf.retrySpec(log))
            .doOnError { t: Throwable -> log.warn("Arena OIDC oppslag feilet", t) }
            .doOnSuccess { log.trace("Arena OIDC oppslag OK, utgår om ${it.expiresIn}s") }
            .block() ?: throw IllegalStateException("Ingen respons fra Arena OIDC")

}