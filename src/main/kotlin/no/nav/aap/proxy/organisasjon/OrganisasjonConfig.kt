package no.nav.aap.proxy.organisasjon

import no.nav.aap.api.felles.OrgNummer
import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.web.util.UriBuilder
import java.net.URI
import org.springframework.web.util.UriComponentsBuilder.newInstance


@ConfigurationProperties(prefix = "organisasjon")
@ConstructorBinding
class OrganisasjonConfig (baseUri: URI,
                          @DefaultValue(V1_ORGANISASJON) private val organisasjonPath: String,
                          @DefaultValue("true") enabled: Boolean) :
    AbstractRestConfig(baseUri, pingPath(organisasjonPath), enabled) {

    fun getOrganisasjonURI(b: UriBuilder, orgnr: OrgNummer) = b.path(organisasjonPath).build(orgnr.orgnr)

    override fun toString(): String {
        return javaClass.simpleName + "[organisasjonPath=" + organisasjonPath + ", pingEndpoint=" + pingEndpoint + "]"
    }

    companion object {
        private const val V1_ORGANISASJON = "v1/organisasjon/{orgnr}"
        private const val TESTORG = "947064649"
        private fun pingPath(organisasjonPath: String): String {
            return newInstance()
                .path(organisasjonPath)
                .build(TESTORG)
                .toString()
        }
    }
}