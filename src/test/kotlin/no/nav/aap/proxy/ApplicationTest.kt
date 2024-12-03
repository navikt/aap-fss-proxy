package no.nav.aap.proxy

import io.mockk.every
import io.mockk.mockk
import no.nav.aap.proxy.arena.rest.ArenaOIDCWebClientAdapter
import no.nav.aap.proxy.arena.rest.ArenaVedtakRestConfig
import no.nav.aap.proxy.sts.OidcToken
import no.nav.aap.util.Constants
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.net.URI
import java.util.function.Function


@SpringBootTest
@EnableAutoConfiguration
class ApplicationTest {

    @TestConfiguration
    class ArenaOidcMock {

        @Bean
        @Primary
        @Qualifier(ArenaVedtakRestConfig.ARENAOIDC)
        fun arenaOidcMock(): WebClient = mockk(relaxed = true) {
            val mono = Mono.just(ArenaOIDCWebClientAdapter.ArenaOidcToken(
                    "test", "test", 1000
                ))
            val spec = mockk<WebClient.RequestBodyUriSpec>(relaxed = true) {
                every { uri(any() as Function<UriBuilder, URI>) } returns this
                every { contentType(any()) } returns this
                every { bodyValue(any()) } returns this
                every {
                    exchangeToMono(any() as Function<ClientResponse, Mono<ArenaOIDCWebClientAdapter.ArenaOidcToken>>)
                } returns mono
            }
            every { post() } returns spec
        }

        @Bean
        @Primary
        @Qualifier(Constants.STS)
        fun stsMock(): WebClient = mockk(relaxed = true) {
            val mono = Mono.just(OidcToken(
                    mockk(relaxed = true), "test", 1000
                ))
            val spec = mockk<WebClient.RequestBodyUriSpec>(relaxed = true) {
                every { uri(any() as Function<UriBuilder, URI>) } returns this
                every {
                    exchangeToMono(any() as Function<ClientResponse, Mono<OidcToken>>)
                } returns mono
            }
            every { get() } returns spec
        }

    }
    
    @Test
    fun initializeContext() {}

}