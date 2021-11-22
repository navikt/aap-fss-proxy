package no.nav.aap.sts

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.security.token.support.core.jwt.JwtToken
import java.time.LocalDateTime

data  class OidcToken(
    @JsonProperty(value = "access_token", required = true)
    val token: JwtToken,
    @JsonProperty(value = "token_type", required = true)
    val type: String,
    @JsonProperty(value = "expires_in", required = true)
    val expiresIn: Int
) {
    private val expirationTime: LocalDateTime = LocalDateTime.now().plusSeconds(expiresIn - 20L)
    fun hasExpired() = expirationTime.isBefore(LocalDateTime.now())

}