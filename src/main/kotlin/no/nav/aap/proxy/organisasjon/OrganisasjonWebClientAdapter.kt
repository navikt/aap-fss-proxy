package no.nav.aap.proxy.organisasjon

import no.nav.aap.api.felles.OrgNummer
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.Constants.ORGANISASJON
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

    fun orgNavn(orgnr: OrgNummer) =
              webClient
                .get()
                .uri { b -> cf.getOrganisasjonURI(b, orgnr) }
                .accept(APPLICATION_JSON)
                .retrieve()
                  .onStatus({ obj: HttpStatus -> obj.isError }) { Mono.empty() }
                  .bodyToMono(OrganisasjonDTO::class.java)
                .doOnError { t: Throwable -> log.warn("Organisasjon oppslag feilet", t) }
                .doOnSuccess { log.trace("Organisasjon oppslag OK") }
                  .mapNotNull(OrganisasjonDTO::fulltNavn)
                  .defaultIfEmpty(orgnr.orgnr)
                  .block()
                  .also { log.trace("Organisasjon oppslag response $it") }

    override fun name() =  capitalize(ORGANISASJON.lowercase(getDefault()))
}