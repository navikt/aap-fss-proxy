package no.nav.aap.joark.controller

import no.nav.aap.config.SecurityConfig.Companion.ISSUER_AAD
import no.nav.security.token.support.spring.ProtectedRestController

@ProtectedRestController(value = ["/joark/aad"], issuer = ISSUER_AAD )
class JoarkAadController {

}
