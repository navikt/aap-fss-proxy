package no.nav.aap.proxy.arena

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.PropertyNamingStrategies.*
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.LocalDateTime
import java.time.LocalDateTime.*
import no.nav.aap.proxy.arena.ArenaOIDCConfig.Companion.ARENAOIDC
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class ArenaOIDCWebClientAdapter(@Qualifier(ARENAOIDC) webClient: WebClient, private val cf: ArenaOIDCConfig) :
    AbstractWebClientAdapter(webClient, cf) {

    var token  = getTheToken()

    fun oidcToken(): String {
        if (token.hasExpired()) {
            log.info("Fornyer token")
            token = getTheToken()
        }
        return token.accessToken
    }

    private fun getTheToken() =
        webClient.post()
            .uri { b -> b.path(cf.tokenPath).build() }
            .contentType(APPLICATION_FORM_URLENCODED)
            .bodyValue("grant_type=client_credentials")
            .retrieve()
            .bodyToMono<ArenaOidcToken>()
            .retryWhen(cfg.retrySpec(log))
            .doOnError { t: Throwable -> log.warn("Arena OIDC oppslag feilet!", t) }
            .doOnSuccess { log.trace("Arena OIDC oppslag OK, utgår om ${it.expiresIn}s") }
            .block() ?: throw IllegalStateException("Ingen respons fra Arena OIDC")

    override fun ping() = mapOf("status" to "OK")  // TODO hvordan pinge denne

    @JsonNaming(SnakeCaseStrategy::class)
    data class ArenaOidcToken(val accessToken: String,
                              val tokenType: String,
                              val expiresIn: Int) {

        private val log = getLogger(javaClass)
        private val createdTime = now()

        fun hasExpired() =
            with(createdTime.plusSeconds(expiresIn.toLong())) {
                now().minusSeconds(30).isAfter(this).also {
                    log.trace("${now().minusSeconds(30)} Token utløper $this -> utløpt = $it")
                }
            }
    }
}