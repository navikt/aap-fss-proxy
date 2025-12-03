package no.nav.aap.proxy.arena

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.proxy.arena.soap.ArenaOpprettOppgaveParams
import no.nav.aap.proxy.arena.soap.BehandleKjoerelisteOgOpprettOppgaveRequest
import no.nav.aap.proxy.arena.soap.HentNyesteAktivSakRequest
import no.nav.aap.util.Constants.AAD
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus

@ProtectedRestController(value = ["/arena"], issuer = AAD, claimMap = [""])
class ArenaController(private val arena: ArenaClient) {

    @PostMapping("/nyesteaktivesak")
    fun nyesteAktiveSakNy(@RequestBody request: HentNyesteAktivSakRequest) =
        arena.nyesteSak(Fødselsnummer(request.personident))?.let { ok(it) } ?: noContent().build()

    @PostMapping("/opprettoppgave")
    @ResponseStatus(CREATED)
    fun opprettOppgave(@RequestBody opgaveInfo: ArenaOpprettOppgaveParams) = arena.opprettOppgave(opgaveInfo)

    @PostMapping("/behandleKjoerelisteOgOpprettOppgave")
    fun behandleKjoerelisteOgOpprettOppgave(@RequestBody request: BehandleKjoerelisteOgOpprettOppgaveRequest) =
        arena.behandleKjoerelisteOgOpprettOppgave(request.journalpostId)

}
