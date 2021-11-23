package no.nav.aap.joark

import no.nav.aap.rest.AbstractPingableHealthIndicator
import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.stereotype.Component

@Component
class JoarkHealthIndicator(adapter: JoarkWebClientAdapter) : AbstractPingableHealthIndicator(adapter) {
}