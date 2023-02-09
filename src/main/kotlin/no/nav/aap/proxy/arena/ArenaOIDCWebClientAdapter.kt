package no.nav.aap.proxy.arena

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.LocalDateTime
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class ArenaOIDCWebClientAdapter(@Qualifier("arenaoidc") webClient: WebClient, cf: ArenaOIDCConfig) :
    AbstractWebClientAdapter(webClient, cf) {

    var token  = getTheToken()

    fun oidcToken(): String {
        if (token.hasExpired()) {
            log.trace("Fornyer token")
            token = getTheToken()
        }
        return token.accessToken!!
    }

    private fun getTheToken() =
        webClient.post()
            .uri { b ->
                b.path("/oauth/token").build()
            }
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("grant_type=client_credentials")
            .retrieve()
            .bodyToMono<ArenaOidcToken>()
            .doOnError { t: Throwable -> log.warn("Arena OIDC oppslag feilet!", t) }
            .doOnSuccess { log.trace("Arena OIDC oppslag OK, utgår om ${it.expiresIn}s") }
            .block() ?: throw IllegalStateException("Ingen respons fra Arena OIDC")

    override fun ping() = mapOf("status" to "OK")  // TODO hvordan pinge denne

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    data class ArenaOidcToken(val accessToken: String? = null,
                              val tokenType: String? = null,
                              val expiresIn: Int? = null) {

        private val log = getLogger(javaClass)
        private val createdTime = LocalDateTime.now()

        fun hasExpired() =
            with(createdTime.plusSeconds(expiresIn!!.toLong())) {
                LocalDateTime.now().minusSeconds(30).isAfter(this).also {
                    log.info("${LocalDateTime.now().minusSeconds(30)} Token utløper $this -> utløpt = $it")
                }
            }
    }
}