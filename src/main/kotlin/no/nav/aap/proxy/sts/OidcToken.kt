package no.nav.aap.proxy.sts

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import no.nav.security.token.support.core.jwt.JwtToken
import java.time.LocalDateTime
@JsonNaming(SnakeCaseStrategy::class)
data  class OidcToken( val accessToken: JwtToken? = null ,
                       val tokenType: String? = null ,
                       val expiresIn: Int? = null) {
    fun hasExpired() = LocalDateTime.now().plusSeconds(expiresIn!! - 20L).isBefore(LocalDateTime.now())
}