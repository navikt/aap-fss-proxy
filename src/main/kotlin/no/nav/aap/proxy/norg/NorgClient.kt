package no.nav.aap.proxy.norg

import org.springframework.stereotype.Component
class NorgClient(private val adapter: NorgWebClientAdapter) {
    fun hentArbeidsfordeling(req: ArbeidRequest)  = adapter.hentArbeidsfordeling(req)
}