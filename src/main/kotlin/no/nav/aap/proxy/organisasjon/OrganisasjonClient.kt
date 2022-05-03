package no.nav.aap.proxy.organisasjon

import no.nav.aap.api.felles.OrgNummer
import org.springframework.stereotype.Component

@Component
class OrganisasjonClient(private val adapter: OrganisasjonWebClientAdapter) {
    fun orgNavn(orgnr: OrgNummer) = adapter.orgNavn(orgnr)
}