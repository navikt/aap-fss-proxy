package no.nav.aap.proxy.organisasjon

import no.nav.aap.proxy.organisasjon.OrganisasjonConfig.Companion.ORGANISASJON
import no.nav.aap.rest.AbstractWebClientAdapter
import org.apache.commons.lang3.StringUtils.capitalize
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.Locale.getDefault

@Component
class OrganisasjonWebClientAdapter(@Qualifier(ORGANISASJON)  val client: WebClient, private val cf: OrganisasjonConfig) : AbstractWebClientAdapter(client, cf) {

    fun orgNavn(orgnr: String) =
              webClient
                .get()
                .uri { b -> cf.getOrganisasjonURI(b, orgnr) }
                .accept(APPLICATION_JSON)
                .retrieve()
                  .onStatus({ obj: HttpStatus -> obj.isError }) { Mono.empty() }
                  .bodyToMono(OrganisasjonDTO::class.java)
                  .mapNotNull(OrganisasjonDTO::fulltNavn)
                  .defaultIfEmpty(orgnr)
                  .block()

    override fun name() =  capitalize(ORGANISASJON.lowercase(getDefault()))
}