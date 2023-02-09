package no.nav.aap.proxy.arena

import io.micrometer.observation.annotation.Observed
import no.nav.aap.api.felles.Fødselsnummer
import org.springframework.stereotype.Component

@Component
@Observed
class ArenaClient(private val a: ArenaWebClientRestAdapter) {
    fun sisteVedtak(fnr: Fødselsnummer) = a.sisteVedtak(fnr.fnr)
}