package no.nav.aap.joark

import no.nav.aap.joark.domain.Journalpost
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class JoarkClient(private val joarkWebClient: WebClient) {

    fun opprettJournalpost(journalpost: Journalpost): String {
        return joarkWebClient.post()
            .contentType(APPLICATION_JSON)
            .bodyValue(journalpost)
            .retrieve()
            .onStatus({ obj: HttpStatus -> obj.isError }) { obj: ClientResponse -> obj.createException() }
            .bodyToMono<JoarkResponse>()
            .block()!!.journalpostId
    }
}