package no.nav.aap.proxy.sts

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import no.nav.aap.util.LoggerUtil
import no.nav.security.token.support.core.jwt.JwtToken
import java.time.LocalDateTime.now
import java.time.LocalDateTime.ofInstant
import java.time.ZoneId.systemDefault
import java.util.Date

@JsonNaming(SnakeCaseStrategy::class)
  data class OidcToken(val accessToken: JwtToken? = null,
                       val tokenType: String? = null,
                       val expiresIn: Int? = null) {

  val log = LoggerUtil.getLogger(javaClass)
    fun hasExpired() =
       with(ofInstant((accessToken?.jwtTokenClaims?.get("exp") as Date).toInstant(), systemDefault())) {
         now().minusSeconds(30).isAfter(this).also {
           log.info("${now().minusSeconds(30)} Token expiry at $this -> expired =  $it")
         }
       }
}