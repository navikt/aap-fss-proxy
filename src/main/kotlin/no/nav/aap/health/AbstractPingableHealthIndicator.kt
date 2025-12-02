package no.nav.aap.health

import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator

abstract class AbstractPingableHealthIndicator(private val pingable : Pingable) : HealthIndicator {

    override fun health() =
        try {
            up(pingable.ping())
        }
        catch (e : Exception) {
            down(e)
        }

    private fun up(status : Map<String, String>) = with(pingable) {
        if (isEnabled()) {
            Health.up()
                .withDetail("endpoint", pingEndpoint()).withDetails(status)
                .build()
        }
        else {
            Health.up()
                .withDetail("endpoint", pingEndpoint())
                .withDetail("status", "disabled")
                .build()
        }
    }

    private fun down(e : Exception) = with(pingable) {
        Health.down()
            .withDetail("endpoint", pingEndpoint())
            .withException(e).build()
    }

    override fun toString() = "${javaClass.simpleName} [pingable=$pingable]"
}