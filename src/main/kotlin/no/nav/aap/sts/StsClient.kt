package no.nav.aap.sts

import no.nav.aap.rest.RetryAware
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
@Component
class StsClient constructor (private val adapter: StsWebClientAdapter): RetryAware {
    internal constructor(webClient: WebClient, cfg: StsConfig) : this(StsWebClientAdapter(webClient,cfg))
    fun oidcToken() = adapter.oidcToken();
}