package no.nav.aap.proxy.organisasjon

import org.springframework.stereotype.Component

@Component
class OrganisasjonClient(private val adapter: OrganisasjonWebClientAdapter) {
    fun orgNavn(orgnr: String?) = adapter.orgNavn(orgnr)
}