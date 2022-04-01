package no.nav.aap.proxy.joark

import no.nav.aap.joark.Journalpost
import org.springframework.stereotype.Component
@Component
class JoarkClient(private val adapter: JoarkWebClientAdapter) {
    fun opprettJournalpost(journalpost: Journalpost)  = adapter.opprettJournalpost(journalpost)
}