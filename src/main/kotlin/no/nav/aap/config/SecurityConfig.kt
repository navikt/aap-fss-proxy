package no.nav.aap.config

import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.context.annotation.Configuration


@Configuration
class SecurityConfig {
    companion object {
        const val ISSUER_AAD = "aad"
    }
}