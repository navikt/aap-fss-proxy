package no.nav.aap.proxy

import java.time.YearMonth
import java.util.UUID
import no.nav.aap.proxy.ArenaOidcMock.Companion.arenaSak
import no.nav.aap.proxy.ArenaOidcMock.Companion.arenaVedtak
import no.nav.aap.proxy.ArenaOidcMock.Companion.fødselsnummer
import no.nav.aap.proxy.ArenaOidcMock.Companion.inntektIdent
import no.nav.aap.proxy.ArenaOidcMock.Companion.inntektResponse
import no.nav.aap.proxy.arena.soap.HentNyesteAktiveSakRequest
import no.nav.aap.proxy.inntektskomponent.InntektRequest
import no.nav.aap.proxy.inntektskomponent.InntektResponse
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.WebTestClient


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@EnableJwtTokenValidation(ignore = ["org.springdoc", "org.springframework"])
@EnableMockOAuth2Server
@Import(ArenaOidcMock::class)
class ApplicationTest {

    @LocalServerPort
    private var port: Int? = 0

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @Test
    fun `skal hente ut nyeste aktive sak fra arena gitt fnr i body`() {
        val response = WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port")
            .build()
            .post()
            .uri("/arena/nyesteaktivesak")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${genererBearerToken()}")
            .bodyValue(HentNyesteAktiveSakRequest(personident = fødselsnummer))
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult()

        assertThat(response.responseBody).isEqualTo(arenaSak)
    }

    @Test
    fun `skal hente ut inntekt`() {
        val request = InntektRequest(
            ident = inntektIdent,
            ainntektsfilter = "",
            formaal = "",
            maanedFom = YearMonth.now(),
            maanedTom = YearMonth.now(),
        )
        
        val response = WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port")
            .build()
            .post()
            .uri("/inntektskomponent/")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${genererBearerToken()}")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody(InntektResponse::class.java)
            .returnResult()
        
        assertThat(response.responseBody).isEqualTo(inntektResponse)
    }

    @Test
    fun `skal feile dersom token er ugyldig ut inntekt`() {
        val request = InntektRequest(
            ident = inntektIdent,
            ainntektsfilter = "",
            formaal = "",
            maanedFom = YearMonth.now(),
            maanedTom = YearMonth.now(),
        )
        
        WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port")
            .build()
            .post()
            .uri("/inntektskomponent/")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${genererUgyldigBearerToken()}")
            .bodyValue(request)
            .exchange()
            .expectStatus().isUnauthorized
    }

    private fun genererBearerToken(): String {
        val clientId = "lokal:aap:aap-fss-proxy"
        return mockOAuth2Server
            .issueToken(
                issuerId = "aad",
                clientId,
                DefaultOAuth2TokenCallback(
                    issuerId = "aad",
                    audience = listOf("aud-localhost"),
                    claims = mapOf(
                        "oid" to UUID.randomUUID().toString(),
                        "azp" to clientId,
                        "name" to "saksbehandler",
                        "NAVIdent" to "saksbehandler"
                    ),
                    expiry = 3600,
                ),
            ).serialize()
    }


    private fun genererUgyldigBearerToken(): String {
        val clientId = "lokal:aap:aap-fss-proxy"
        return mockOAuth2Server
            .issueToken(
                issuerId = "aad",
                clientId,
                DefaultOAuth2TokenCallback(
                    issuerId = "aad",
                    audience = listOf("ugyldig-aud"),
                    claims = mapOf(
                        "oid" to UUID.randomUUID().toString(),
                        "azp" to clientId,
                        "name" to "saksbehandler",
                        "NAVIdent" to "saksbehandler"
                    ),
                    expiry = 3600,
                ),
            ).serialize()
    }


}