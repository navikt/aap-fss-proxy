package no.nav.aap.proxy.arena

import no.nav.aap.api.felles.Fødselsnummer
import org.apache.coyote.http11.Constants.a
import org.springframework.stereotype.Component

@Component
class ArenaClient(private val rest: ArenaWebClientRestAdapter, val soap: ArenaWebServiceAdapter) {
    fun sisteVedtak(fnr: Fødselsnummer) = rest.sisteVedtak(fnr.fnr)

    fun harAktivSak(fnr: Fødselsnummer) = soap.hentSaker(fnr).isNotEmpty()
}