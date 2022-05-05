package no.nav.aap.proxy.norg

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController("/norg")
class NorgController(private val norg: NorgClient) {

    @PostMapping("/arbeidsfordeling")
    fun hentArbeidsfordeling(@RequestBody request: ArbeidRequest) = norg.hentArbeidsfordeling(request)
}