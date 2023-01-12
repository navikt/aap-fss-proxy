package no.nav.aap.proxy.arena

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import no.nav.aap.util.LoggerUtil
import java.time.LocalDateTime

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ArenaOidcToken(val accessToken: String? = null,
                     val tokenType: String? = null,
                     val expiresIn: Int? = null) {

    private val log = LoggerUtil.getLogger(javaClass)
    private val createdTime = LocalDateTime.now()

    fun hasExpired() =
        with(
            createdTime.plusSeconds(expiresIn!!.toLong())
        ) {
            LocalDateTime.now().minusSeconds(30).isAfter(this).also {
                log.info("${LocalDateTime.now().minusSeconds(30)} Token utløper $this -> utløpt = $it")
            }
        }
}