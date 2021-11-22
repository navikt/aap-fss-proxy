package no.nav.aap.joark

import no.nav.aap.config.Constants.JOARK
import no.nav.aap.joark.domain.Journalpost
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
@Component
class JoarkWebClientAdapter internal constructor(@Qualifier(JOARK) webClient: WebClient, cfg: JoarkConfig) : AbstractWebClientAdapter(webClient, cfg) {
    fun opprettJournalpost(journalpost: Journalpost): String {
        return webClient.post()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(journalpost)
            .retrieve()
            .onStatus({ obj: HttpStatus -> obj.isError }) { obj: ClientResponse -> obj.createException() }
            .bodyToMono<JoarkResponse>()
            .block()!!.journalpostId
    }
}