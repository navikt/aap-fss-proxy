package no.nav.aap.sts

import no.nav.aap.rest.AbstractPingableHealthIndicator
import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.stereotype.Component

@Component
class StsHealthIndicator(adapter: StsWebClientAdapter) : AbstractPingableHealthIndicator(adapter) {
}