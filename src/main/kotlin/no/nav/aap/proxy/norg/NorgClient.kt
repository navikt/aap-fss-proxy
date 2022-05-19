package no.nav.aap.proxy.norg

import org.springframework.stereotype.Component

@Component
class NorgClient(private val a: NorgWebClientAdapter) {
    fun hentArbeidsfordeling(req: ArbeidRequest)  = a.hentArbeidsfordeling(req)
}