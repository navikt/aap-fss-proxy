package no.nav.aap.proxy.arena

import org.springframework.stereotype.Component


@Component
class ArenaOIDCClient (private val a: ArenaOIDCWebClientAdapter)  {
    fun oidcToken() = a.oidcToken()
}