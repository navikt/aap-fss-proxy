package no.nav.aap.proxy.sts

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import no.nav.aap.util.LoggerUtil
import no.nav.security.token.support.core.jwt.JwtToken
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

//@JsonNaming(SnakeCaseStrategy::class)
  data class OidcToken(@JsonAlias("access_token") val accessToken: JwtToken? = null ,
                       @JsonAlias("token_type") val tokenType: String? = null ,
                       @JsonAlias("expires_in") val expiresIn: Int? = null) {

  val log = LoggerUtil.getLogger(javaClass)
    fun hasExpired() =
       with(accessToken?.jwtTokenClaims?.get("exp") as Date) {
         Date().after(this).also {
           val ldt = LocalDateTime.ofInstant(this.toInstant(), ZoneId.systemDefault())
           log.info("LDT expiry $ldt, now er ${LocalDateTime.now()}")
           log.info("Token expiry at $this -> expired =  $it")
         }
       }
}