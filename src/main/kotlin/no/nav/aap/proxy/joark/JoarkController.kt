package no.nav.aap.proxy.joark

import no.nav.aap.joark.JoarkResponse
import no.nav.aap.joark.Journalpost
import no.nav.aap.util.Constants.AAD
import no.nav.security.token.support.spring.ProtectedRestController
import org.slf4j.LoggerFactory.getLogger
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody


@ProtectedRestController(value = ["/joark"], issuer = AAD, claimMap =[""])
class JoarkController(private val joark: JoarkClient) {

    private val log = getLogger(javaClass)

    @PostMapping("/aad")
    fun opprettJournalpostFraSaksbehandler(@RequestBody journalpost: Journalpost) : JoarkResponse?{
        log.trace("oppretter journalpost $journalpost")
        val res =  joark.opprettJournalpost(journalpost)
        log.trace("opprettet journalpost OK $res")
        return res;
    }
}