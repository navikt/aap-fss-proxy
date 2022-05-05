package no.nav.aap.proxy.norg

import no.nav.aap.joark.Journalpost
import no.nav.aap.proxy.joark.JoarkWebClientAdapter
import org.springframework.stereotype.Component
@Component
class NorgClient(private val adapter: NorgWebClientAdapter) {
    fun hentArbeidsfordeling(request: ArbeidRequest)  = adapter.hentArbeidsfordeling(request)
}