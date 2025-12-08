package no.nav.aap.proxy

import io.mockk.every
import io.mockk.mockk
import java.net.URI
import java.time.YearMonth
import java.util.function.Function
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.proxy.arena.rest.ArenaOIDCWebClientAdapter
import no.nav.aap.proxy.arena.rest.ArenaVedtakRestConfig
import no.nav.aap.proxy.arena.soap.ArenaSakSoapAdapter
import no.nav.aap.proxy.inntektskomponent.InntektIdent
import no.nav.aap.proxy.inntektskomponent.InntektResponse
import no.nav.aap.proxy.inntektskomponent.InntektWebClientAdapter
import no.nav.aap.proxy.sts.OidcToken
import no.nav.aap.util.Constants
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono

@TestConfiguration
class ArenaOidcMock {

    @Bean
    @Primary
    @Qualifier(ArenaVedtakRestConfig.ARENAOIDC)
    fun arenaOidcMock(): WebClient = mockk(relaxed = true) {
        val mono = Mono.just(
            ArenaOIDCWebClientAdapter.ArenaOidcToken(
                "test", "test", 1000
            )
        )
        val spec = mockk<WebClient.RequestBodyUriSpec>(relaxed = true) {
            every { uri(any() as java.util.function.Function<UriBuilder, URI>) } returns this
            every { contentType(any()) } returns this
            every { bodyValue(any()) } returns this
            every {
                exchangeToMono(any() as java.util.function.Function<ClientResponse, Mono<ArenaOIDCWebClientAdapter.ArenaOidcToken>>)
            } returns mono
        }
        every { post() } returns spec
    }

    @Bean
    @Primary
    @Qualifier(Constants.STS)
    fun stsMock(): WebClient = mockk(relaxed = true) {
        val mono = Mono.just(
            OidcToken(
                mockk(relaxed = true), "test", 1000
            )
        )
        val spec = mockk<WebClient.RequestBodyUriSpec>(relaxed = true) {
            every { uri(any() as java.util.function.Function<UriBuilder, URI>) } returns this
            every {
                exchangeToMono(any() as Function<ClientResponse, Mono<OidcToken>>)
            } returns mono
        }
        every { get() } returns spec
    }

    @Bean
    @Primary
    fun arenaSakSoapAdapter(): ArenaSakSoapAdapter {
        val arenaSakSoapAdapter = mockk<ArenaSakSoapAdapter>()
        every { arenaSakSoapAdapter.nyesteAktiveSak(any()) } returns arenaSak
        return arenaSakSoapAdapter
    }

    @Bean
    @Primary
    fun inntektWebClientAdapter(): InntektWebClientAdapter {
        val inntektAdapter = mockk<InntektWebClientAdapter>()

        every { inntektAdapter.getInntekt(any()) } returns inntektResponse
        return inntektAdapter
    }

    companion object {
        val arenaVedtak = "abc"
        val arenaSak = "SAK12345"
        val fødselsnummer = "19819699677"
        val inntektIdent = InntektIdent(
            identifikator = Fødselsnummer(fødselsnummer),
            aktoerType = InntektIdent.AktørType.NATURLIG_IDENT
        )
        val inntektResponse = InntektResponse(
            arbeidsInntektMaaned = listOf(
                InntektResponse.Måned(
                    YearMonth.now(), InntektResponse.ArbeidsInntektInformasjon(
                        listOf(
                            InntektResponse.ArbeidsInntektInformasjon.Inntekt(1000.0),
                            InntektResponse.ArbeidsInntektInformasjon.Inntekt(2000.0)
                        )
                    )
                )
            ),
            ident = inntektIdent
        )
    }
}
