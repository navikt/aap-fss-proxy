package no.nav.aap.proxy.joark

import no.nav.aap.joark.Journalpost
import org.springframework.stereotype.Component
@Component
class JoarkClient(private val a: JoarkWebClientAdapter) {
    fun opprettJournalpost(p: Journalpost)  = a.opprettJournalpost(p)
    fun ping()= a.ping()
}