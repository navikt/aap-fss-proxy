package no.nav.aap.joark

import no.nav.aap.error.IntegrationException
import no.nav.aap.joark.domain.Journalpost
import no.nav.aap.sts.StsClient
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class JoarkClient(
    private val joarkWebClient: WebClient) {

    fun opprettJournalpost(journalpost: Journalpost): String {
        val response = joarkWebClient.post()
            .contentType(APPLICATION_JSON)
            .bodyValue(journalpost)
            .retrieve()
            .onStatus({ obj: HttpStatus -> obj.isError }) { obj: ClientResponse -> obj.createException() }
            .bodyToMono<JoarkResponse>()
            .block()
        return response?.journalpostId ?: throw IntegrationException("Kunne ikke opprette journalpost")
    }
}