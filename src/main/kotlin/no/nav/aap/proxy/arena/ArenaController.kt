package no.nav.aap.proxy.arena

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.proxy.arena.soap.ArenaOpprettOppgaveParams
import no.nav.aap.proxy.arena.soap.BehandleKjoerelisteOgOpprettOppgaveRequest
import no.nav.aap.util.Constants.AAD
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*

@ProtectedRestController(value = ["/arena"], issuer = AAD, claimMap = [""])
class ArenaController(private val arena : ArenaClient) {

    private val log = getLogger(javaClass)

    @GetMapping("/vedtak")
    fun sisteVedtakNy(@RequestHeader("personident") personident: String) = arena.sisteVedtak(Fødselsnummer(personident))

    @GetMapping("/nyesteaktivesak")
    fun nyesteAktiveSakNy(@RequestHeader("personident") personident: String) = arena.nyesteSak(Fødselsnummer(personident))?.let { ok(it) } ?: noContent().build()

    @Deprecated("Fjern når disse ikke brukes i soknad-api pga fnr i path")
    @GetMapping("/vedtak/{fnr}")
    fun sisteVedtak(@PathVariable fnr : Fødselsnummer) = arena.sisteVedtak(fnr)

    @Deprecated("Fjern når disse ikke brukes i soknad-api pga fnr i path")
    @GetMapping("/nyesteaktivesak/{fnr}")
    fun nyesteAktiveSak(@PathVariable fnr: Fødselsnummer): ResponseEntity<String>  {
        log.info("Henter nyeste aktive sak.")
        val res = arena.nyesteSak(fnr)?.let { ok(it) } ?: noContent().build()
        log.info("Hentet nyeste aktive sak. Respons: ${res.statusCode}")
        return res
    }

    @PostMapping("/opprettoppgave")
    @ResponseStatus(CREATED)
    fun opprettOppgave(@RequestBody opgaveInfo : ArenaOpprettOppgaveParams) = arena.opprettOppgave(opgaveInfo)

    @PostMapping("/behandleKjoerelisteOgOpprettOppgave")
    fun behandleKjoerelisteOgOpprettOppgave(@RequestBody request: BehandleKjoerelisteOgOpprettOppgaveRequest) =
        arena.behandleKjoerelisteOgOpprettOppgave(request.journalpostId)

}
