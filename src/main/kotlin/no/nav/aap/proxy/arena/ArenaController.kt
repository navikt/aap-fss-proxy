package no.nav.aap.proxy.arena

import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.proxy.arena.soap.ArenaOpprettOppgaveParams
import no.nav.aap.proxy.arena.soap.BehandleKjoerelisteOgOpprettOppgaveRequest
import no.nav.aap.util.Constants.AAD
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.RequestHeader

@ProtectedRestController(value = ["/arena"], issuer = AAD, claimMap = [""])
class ArenaController(private val arena : ArenaClient) {

    @GetMapping("/vedtak")
    fun sisteVedtakNy(@RequestHeader("personident") personident: String) = arena.sisteVedtak(Fødselsnummer(personident))

    @GetMapping("/nyesteaktivesak")
    fun nyesteAktiveSakNy(@RequestHeader("personident") personident: String) = arena.nyesteSak(Fødselsnummer(personident))?.let { ok(it) } ?: noContent().build()

    @Deprecated("Fjern når disse ikke brukes i soknad-api pga fnr i path")
    @GetMapping("/vedtak/{fnr}")
    fun sisteVedtak(@PathVariable fnr : Fødselsnummer) = arena.sisteVedtak(fnr)

    @Deprecated("Fjern når disse ikke brukes i soknad-api pga fnr i path")
    @GetMapping("/nyesteaktivesak/{fnr}")
    fun nyesteAktiveSak(@PathVariable fnr : Fødselsnummer) = arena.nyesteSak(fnr)?.let { ok(it) } ?: noContent().build()

    @PostMapping("/opprettoppgave")
    @ResponseStatus(CREATED)
    fun opprettOppgave(@RequestBody opgaveInfo : ArenaOpprettOppgaveParams) = arena.opprettOppgave(opgaveInfo)

    @PostMapping("/behandleKjoerelisteOgOpprettOppgave")
    fun behandleKjoerelisteOgOpprettOppgave(@RequestBody request: BehandleKjoerelisteOgOpprettOppgaveRequest) =
        arena.behandleKjoerelisteOgOpprettOppgave(request.journalpostId)
}