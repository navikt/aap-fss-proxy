package no.nav.aap.proxy

import java.time.YearMonth
import java.util.UUID
import no.nav.aap.proxy.ArenaOidcMock.Companion.arenaSak
import no.nav.aap.proxy.ArenaOidcMock.Companion.arenaVedtak
import no.nav.aap.proxy.ArenaOidcMock.Companion.fødselsnummer
import no.nav.aap.proxy.ArenaOidcMock.Companion.inntektIdent
import no.nav.aap.proxy.ArenaOidcMock.Companion.inntektResponse
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
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.exchange


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
    fun `skal hente ut nyeste aktive sak fra arena gitt fnr i url`() {
        val headers = HttpHeaders()
        headers.setBearerAuth(genererBearerToken())

        val response: ResponseEntity<String> = TestRestTemplate().restTemplate.exchange(
            "http://localhost:$port/arena/nyesteaktivesak/$fødselsnummer",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(arenaSak)
    }

    @Test
    fun `skal hente ut nyeste aktive sak fra arena gitt fnr i header`() {
        val headers = HttpHeaders()
        headers.setBearerAuth(genererBearerToken())
        headers.add("personident", fødselsnummer)
        val response: ResponseEntity<String> = TestRestTemplate().restTemplate.exchange(
            "http://localhost:$port/arena/nyesteaktivesak",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(arenaSak)
    }

    @Test
    fun `skal hente ut nyeste vedtak fra arena gitt fnr i header`() {
        val headers = HttpHeaders()
        headers.setBearerAuth(genererBearerToken())
        headers.add("personident", fødselsnummer)
        val response: ResponseEntity<String> = TestRestTemplate().restTemplate.exchange(
            "http://localhost:$port/arena/vedtak",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(arenaVedtak)
    }

    @Test
    fun `skal hente ut nyeste vedtak fra arena gitt fnr i url`() {
        val headers = HttpHeaders()
        headers.setBearerAuth(genererBearerToken())
        val response: ResponseEntity<String> = TestRestTemplate().restTemplate.exchange(
            "http://localhost:$port/arena/vedtak/$fødselsnummer",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(arenaVedtak)
    }

    @Test
    fun `skal hente ut inntekt`() {
        val headers = HttpHeaders()
        headers.setBearerAuth(genererBearerToken())
        val entity = HttpEntity(
            InntektRequest(
                ident = inntektIdent,
                ainntektsfilter = "",
                formaal = "",
                maanedFom = YearMonth.now(),
                maanedTom = YearMonth.now(),
            ), headers
        )
        val response: ResponseEntity<InntektResponse> = TestRestTemplate().restTemplate.exchange<InntektResponse>(
            "http://localhost:$port/inntektskomponent/",
            HttpMethod.POST,
            entity,
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(inntektResponse)
    }

    @Test
    fun `skal feile dersom token er ugyldig ut inntekt`() {
        val headers = HttpHeaders()
        headers.setBearerAuth(genererUgyldigBearerToken())
        val entity = HttpEntity(
            InntektRequest(
                ident = inntektIdent,
                ainntektsfilter = "",
                formaal = "",
                maanedFom = YearMonth.now(),
                maanedTom = YearMonth.now(),
            ), headers
        )
        val response: ResponseEntity<Any> = TestRestTemplate().restTemplate.exchange(
            "http://localhost:$port/inntektskomponent/",
            HttpMethod.POST,
            entity,
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
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