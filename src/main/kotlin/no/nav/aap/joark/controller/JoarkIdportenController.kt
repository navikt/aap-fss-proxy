package no.nav.aap.joark.controller

import no.nav.aap.config.SecurityConfig.Companion.ISSUER_IDPORTEN
import no.nav.security.token.support.spring.ProtectedRestController

@ProtectedRestController(value = ["/joark/idporten"], issuer = ISSUER_IDPORTEN )
class JoarkIdportenController {
}
