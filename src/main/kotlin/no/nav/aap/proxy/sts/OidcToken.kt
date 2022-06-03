package no.nav.aap.proxy.sts

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import no.nav.aap.util.LoggerUtil
import no.nav.security.token.support.core.jwt.JwtToken
import java.util.Date

@JsonNaming(SnakeCaseStrategy::class)
  data class OidcToken(val accessToken: JwtToken? = null ,
                       val tokenType: String? = null ,
                       val expiresIn: Int? = null) {

  val log = LoggerUtil.getLogger(javaClass)
    fun hasExpired(): Boolean {
      val date = this.accessToken?.jwtTokenClaims?.get("exp") as Date
      val now = Date()
      val expired = now.after(date)
      log.info("Token expiry at $date now er $now expired $expired")
      return expired
    }
}