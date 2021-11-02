package no.nav.aap.joark.client

import no.nav.aap.joark.domain.JoarkResponse
import no.nav.aap.joark.domain.Journalpost
import no.nav.aap.sts.StsClient
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class JoarkClient(
    private val joarkWebClient: WebClient,
    private val stsClient: StsClient
) {

    fun opprettJournalpost(journalpost: Journalpost): String {
        val response = joarkWebClient.post()
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${stsClient.oidcToken()}")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(journalpost)
            .retrieve()
            .bodyToMono(JoarkResponse::class.java)
            .block()
            ?: throw RuntimeException("Klarte ikke opprette journalpost.")

        return response.journalpostId
    }

}
