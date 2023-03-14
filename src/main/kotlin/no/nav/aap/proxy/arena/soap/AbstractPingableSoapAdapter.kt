package no.nav.aap.proxy.arena.soap

import no.nav.aap.health.Pingable

abstract class AbstractPingableSoapAdapter(protected val cfg: ArenaSoapConfig) : Pingable {
    override fun isEnabled() = cfg.enabled
    override fun name() = javaClass.simpleName
}