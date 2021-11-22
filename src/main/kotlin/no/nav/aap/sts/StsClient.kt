package no.nav.aap.sts

import no.nav.aap.rest.RetryAware
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class StsClient(private val adapter: StsWebClientAdapter): RetryAware {
    fun oidcToken() = adapter.oidcToken();
}