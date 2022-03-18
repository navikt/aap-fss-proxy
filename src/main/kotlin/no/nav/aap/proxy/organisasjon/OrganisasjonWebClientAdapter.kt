package no.nav.aap.proxy.organisasjon

import no.nav.aap.proxy.organisasjon.OrganisasjonConfig.Companion.ORGANISASJON
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.LoggerUtil
import org.apache.commons.lang3.StringUtils.capitalize
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.util.*

@Component
class OrganisasjonWebClientAdapter(@Qualifier(ORGANISASJON)  val client: WebClient, private val cf: OrganisasjonConfig) : AbstractWebClientAdapter(client, cf) {
    private val log = LoggerUtil.getLogger(javaClass)



    
    //@Cacheable(cacheNames = ["organisasjon"])
    fun orgNavn(orgnr: String) : String {
             var n = webClient
                .get()
                .uri { b -> cf.getOrganisasjonURI(b, orgnr) }
                .accept(APPLICATION_JSON)
                .retrieve()
                 .bodyToMono(OrganisasjonDTO::class.java)
                .block()
                log.trace("Orgnavn $n")
                val fn =  n?.fulltNavn() ?: orgnr
                log.trace("Fullt Orgnavn $fn")
                return fn
    }
    override fun name() =  capitalize(ORGANISASJON.lowercase(Locale.getDefault()))
}