package no.nav.aap.proxy.arbeidsforhold

import org.springframework.stereotype.Component

@Component
class ArbeidsforholdClient(private val adapter: ArbeidsforholdClientAdapter)  {
    fun arbeidsforhold() = adapter.arbeidsforhold()
}