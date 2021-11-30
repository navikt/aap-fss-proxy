package no.nav.aap.proxy.sts

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import javax.inject.Inject

@Component
class StsClient @Inject constructor(private val adapter: StsWebClientAdapter)  {
      constructor(webClient: WebClient, cfg: StsConfig) : this(StsWebClientAdapter(webClient,cfg))
    fun oidcToken() = adapter.oidcToken();
}