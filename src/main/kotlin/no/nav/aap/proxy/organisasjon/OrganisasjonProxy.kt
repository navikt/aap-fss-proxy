package no.nav.aap.proxy.organisasjon

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.proxy.organisasjon.OrganisasjonClientBeanConfig.OrganisasjonClient
import no.nav.aap.proxy.organisasjon.OrganisasjonConfig.Companion.ORGANISASJON
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.aap.util.Constants
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.EnvUtil
import no.nav.security.token.support.core.api.Unprotected
import no.nav.security.token.support.spring.ProtectedRestController
import org.apache.commons.lang3.StringUtils.capitalize
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.net.URI
import java.util.Locale.getDefault


@ProtectedRestController(value = ["/organisasjon"], issuer = Constants.IDPORTEN)
class OrganisasjonController(val client: OrganisasjonClient) {

    private  val NAV = "998004993"
    private val log = LoggerUtil.getLogger(javaClass)
    @GetMapping
    fun navn(@RequestParam("orgnummer") orgnummer: String) = client.orgNavn(orgnummer)

    @GetMapping("/ping")
    @Unprotected
    fun ping() = navn(NAV)
}

@Configuration
class OrganisasjonClientBeanConfig(@Value("\${spring.application.name}") val applicationName: String) {
    @Bean
    @Qualifier(ORGANISASJON)
    fun organisasjonWebClient(builder: WebClient.Builder, cfg: OrganisasjonConfig, env: Environment) =
        builder
            .clientConnector(ReactorClientHttpConnector(HttpClient.create().wiretap(EnvUtil.isDevOrLocal(env))))
            .baseUrl("${cfg.baseUri}")
            .filter(correlatingFilterFunction(applicationName))
            .build()

    @Component
    class OrganisasjonClient(private val a: OrganisasjonWebClientAdapter) {
        fun orgNavn(orgnr: String) = a.orgNavn(orgnr)
    }
    @Component
    class OrganisasjonWebClientAdapter(@Qualifier(ORGANISASJON)  private val client: WebClient, private val cf: OrganisasjonConfig) : AbstractWebClientAdapter(client, cf) {

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
                .also {  log.trace("Organisasjonsnavn oppslag response er $it")}

        override fun name() = capitalize(ORGANISASJON.lowercase(getDefault()))
    }
    @Bean
    fun organisasjonHealthIndicator(a: OrganisasjonWebClientAdapter) = object : AbstractPingableHealthIndicator(a){}
}
@ConfigurationProperties(prefix = "organisasjon")
@ConstructorBinding
class OrganisasjonConfig(baseUri: URI,
                         @DefaultValue(V1_ORGANISASJON) private val organisasjonPath: String,
                         @DefaultValue("true") enabled: Boolean) : AbstractRestConfig(baseUri, pingPath(organisasjonPath), enabled) {

    fun getOrganisasjonURI(b: UriBuilder, orgnr: String) = b.path(organisasjonPath).build(orgnr)

    override fun toString() = javaClass.simpleName + "[organisasjonPath=" + organisasjonPath + ", pingEndpoint=" + pingEndpoint + "]"

    companion object {
        const val ORGANISASJON = "Organisasjon"
        private const val V1_ORGANISASJON = "v1/organisasjon/{orgnr}"
        private const val TESTORG = "947064649"
        private fun pingPath(organisasjonPath: String): String {
            return UriComponentsBuilder.newInstance()
                .path(organisasjonPath)
                .build(TESTORG)
                .toString()
        }
    }
}
@JsonIgnoreProperties(ignoreUnknown = true)
data class OrganisasjonDTO(val navn : OrganisasjonNavnDTO) {
    val fulltNavn =  listOfNotNull(navn.navnelinje1,navn.navnelinje2,navn.navnelinje3,navn.navnelinje4,navn.navnelinje5).joinToString(" ")
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class OrganisasjonNavnDTO(val navnelinje1: String?,val navnelinje2: String?,val navnelinje3: String?,val navnelinje4: String?,val navnelinje5: String?)
}