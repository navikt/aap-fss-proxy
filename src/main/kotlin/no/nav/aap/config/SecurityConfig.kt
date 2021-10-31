package no.nav.aap.config

import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.context.annotation.Configuration

@EnableJwtTokenValidation
@EnableOAuth2Client(cacheEnabled = true)
@Configuration
class SecurityConfig {
    companion object {
        const val ISSUER_AAD = "aad"
        const val ISSUER_IDPORTEN = "idporten"
    }
}
