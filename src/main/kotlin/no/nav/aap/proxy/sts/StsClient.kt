package no.nav.aap.proxy.sts

import javax.inject.Inject
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class StsClient @Inject constructor(private val a: StsWebClientAdapter)  {
      constructor(webClient: WebClient, cfg: StsConfig) : this(StsWebClientAdapter(webClient,cfg))
    fun oidcToken() = a.oidcToken()
}