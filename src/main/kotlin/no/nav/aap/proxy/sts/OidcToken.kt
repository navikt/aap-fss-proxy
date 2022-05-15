package no.nav.aap.proxy.sts

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import no.nav.security.token.support.core.jwt.JwtToken
import java.time.LocalDateTime
@JsonNaming(SnakeCaseStrategy::class)
  data class OidcToken(@JsonAlias("access_token") val accessToken: JwtToken? = null ,
                       @JsonAlias("token_type") val tokenType: String? = null ,
                       @JsonAlias("expires_in")  val expiresIn: Int? = null) {
    fun hasExpired() = LocalDateTime.now().plusSeconds(expiresIn!! - 20L).isBefore(LocalDateTime.now())

}