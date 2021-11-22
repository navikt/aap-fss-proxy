package no.nav.aap.joark

import no.nav.aap.joark.domain.Journalpost
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class JoarkClient(private val adapter: JoarkWebClientAdapter) {
    fun opprettJournalpost(journalpost: Journalpost)  = adapter.opprettJournalpost(journalpost)
}