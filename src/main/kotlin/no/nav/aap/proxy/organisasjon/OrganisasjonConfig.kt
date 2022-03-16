package no.nav.aap.proxy.organisasjon

import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.web.util.UriBuilder
import java.net.URI
import org.springframework.web.util.UriComponentsBuilder.newInstance


@ConfigurationProperties(prefix = "organisasjon")
@ConstructorBinding
class OrganisasjonConfig (@DefaultValue(DEFAULT_BASE_URI) baseUri: URI,
                                                         @DefaultValue(V1_ORGANISASJON) private val organisasjonPath: String,
                                                         @DefaultValue("true") enabled: Boolean) :
    AbstractRestConfig(baseUri, pingPath(organisasjonPath), enabled) {
    fun getOrganisasjonURI(b: UriBuilder, orgnr: String?): URI {
        return b.path(organisasjonPath)
            .queryParam(HISTORIKK, true)
            .build(orgnr)
    }

    override fun toString(): String {
        return javaClass.simpleName + "[organisasjonPath=" + organisasjonPath + ", pingEndpoint=" + pingEndpoint + "]"
    }

    companion object {
        const val ORGANISASJON = "Organisasjon"
        private const val HISTORIKK = "historikk"
        private const val V1_ORGANISASJON = "/v1/organisasjon/{orgnr}"
        private const val NAV = "998004993"
        private const val DEFAULT_BASE_URI = "http://must.be.set"
        private fun pingPath(organisasjonPath: String): String {
            return newInstance()
                .path(organisasjonPath)
                .build(NAV)
                .toString()
        }
    }
}