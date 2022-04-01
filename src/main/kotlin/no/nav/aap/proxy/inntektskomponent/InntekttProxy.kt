package no.nav.aap.proxy.inntektskomponent

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.OrgNummer
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.proxy.inntektskomponent.InntektClientBeanConfig.InntektClient
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.consumerFilterFunction
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.aap.util.Constants
import no.nav.aap.util.Constants.INNTEKTSKOMPONENT
import no.nav.boot.conditionals.EnvUtil
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.netty.http.client.HttpClient
import java.net.URI
import java.time.YearMonth


@ProtectedRestController(value = ["/inntektskomponent"], issuer = Constants.AAD, claimMap =[""])
class InntektskomponentController(private val client: InntektClient) {
    @PostMapping("/")
    fun getInntekt(@RequestBody request: InntektRequest) = client.getInntekt(request)
}

@Configuration
class InntektClientBeanConfig(@Value("\${spring.application.name}") val applicationName: String, val cfg: InntektConfig) {

    @Bean
    @Qualifier(INNTEKTSKOMPONENT)
    fun inntektWebClient(builder: WebClient.Builder, stsExchangeFilterFunction: ExchangeFilterFunction, env: Environment) =
        builder.baseUrl("${cfg.baseUri}")
            .clientConnector(ReactorClientHttpConnector(HttpClient.create().wiretap(EnvUtil.isDevOrLocal(env))))
            .filter(correlatingFilterFunction(applicationName))
            .filter(consumerFilterFunction())
            .filter(stsExchangeFilterFunction)
            .build()

    @Bean
    fun inntektHealthIndicator(a: InntektWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}

    @Component
    class InntektClient(private val a: InntektWebClientAdapter) {
        fun getInntekt(request: InntektRequest) = a.getInntekt(request)
    }
    @Component
    class InntektWebClientAdapter(@Qualifier(INNTEKTSKOMPONENT) webClient: WebClient, private val cf: InntektConfig) : AbstractWebClientAdapter(webClient, cf) {

        fun getInntekt(request: InntektRequest) =
            webClient
                .post()
                .uri { b -> b.path(cf.path).build() }
                .contentType(APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono<InntektResponse>()
                .doOnError { t: Throwable -> log.warn("Inntekts oppslag $request feilet", t) }
                .block()
                .also {  log.trace("Inntekt response er $it")}

    }
}

@ConfigurationProperties("inntektskomponent")
@ConstructorBinding
class InntektConfig(baseUri: URI,
                    @DefaultValue("api/v1/hentinntektliste") val path: String,
                    @DefaultValue("api/ping") pingPath: String,
                    @DefaultValue("true") enabled: Boolean): AbstractRestConfig(baseUri,pingPath,enabled)


data class InntektRequest(val ident: InntektIdent, val ainntektsfilter: String, val formaal: String, val maanedFom: YearMonth, val maanedTom: YearMonth)
data class InntektIdent(val identifikator: String, val aktoerType: String)
data class InntektResponse(val arbeidsInntektMaaned: List<Måned>)
data class Måned(val årMåned: YearMonth, val arbeidsforholdliste: List<Arbeidsforhold>, val inntektsliste: List<Inntekt>)
data class Arbeidsforhold(val type: String?, val orgnummer: OrgNummer?)
data class Inntekt(val beløp: Double, val inntektstype: Inntektstype, val orgnummer: OrgNummer?, val fødselsnummer: Fødselsnummer?, val aktørId: String?, val beskrivelse: String?, val fordel: String?)
enum class Inntektstype { LOENNSINNTEKT, NAERINGSINNTEKT, PENSJON_ELLER_TRYGD, YTELSE_FRA_OFFENTLIGE
}