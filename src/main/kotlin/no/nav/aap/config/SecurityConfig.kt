package no.nav.aap.config

import org.springframework.context.annotation.Configuration


@Configuration
class SecurityConfig {
    companion object {
        const val ISSUER_AAD = "aad"
    }
}