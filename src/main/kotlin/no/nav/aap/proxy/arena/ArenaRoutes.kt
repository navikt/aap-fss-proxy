package no.nav.aap.proxy.arena

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.aap.proxy.arena.rest.ArenaVedtakClient
import no.nav.aap.proxy.arena.soap.ArenaOppgaveClient
import no.nav.aap.proxy.arena.soap.ArenaSakClient
import no.nav.aap.proxy.arena.soap.SakOgAktivitetClient
import no.nav.aap.proxy.config.AppConfig
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("ArenaRoutes")

fun Route.arenaRoutes(config: AppConfig) {
    val vedtakClient = ArenaVedtakClient(config)
    val sakClient = ArenaSakClient(config)
    val oppgaveClient = ArenaOppgaveClient(config)
    val sakOgAktivitetClient = SakOgAktivitetClient(config)
    
    route("/arena") {
        // New endpoints with fnr in header
        get("/vedtak") {
            val personident = call.request.header("personident")
                ?: throw IllegalArgumentException("Missing personident header")
            val fnr = Fødselsnummer(personident)
            val result = vedtakClient.sisteVedtak(fnr.fnr)
            call.respondBytes(result, ContentType.Application.OctetStream)
        }
        
        get("/nyesteaktivesak") {
            val personident = call.request.header("personident")
                ?: throw IllegalArgumentException("Missing personident header")
            val fnr = Fødselsnummer(personident)
            val result = sakClient.nyesteAktiveSak(fnr.fnr)
            if (result != null) {
                call.respondText(result, ContentType.Text.Plain)
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }
        
        // Deprecated endpoints with fnr in path
        get("/vedtak/{fnr}") {
            val fnr = Fødselsnummer(call.parameters["fnr"]!!)
            val result = vedtakClient.sisteVedtak(fnr.fnr)
            call.respondBytes(result, ContentType.Application.OctetStream)
        }
        
        get("/nyesteaktivesak/{fnr}") {
            log.info("Henter nyeste aktive sak.")
            val fnr = Fødselsnummer(call.parameters["fnr"]!!)
            val result = sakClient.nyesteAktiveSak(fnr.fnr)
            if (result != null) {
                log.info("Hentet nyeste aktive sak. Respons: OK")
                call.respondText(result, ContentType.Text.Plain)
            } else {
                log.info("Hentet nyeste aktive sak. Respons: NoContent")
                call.respond(HttpStatusCode.NoContent)
            }
        }
        
        post("/opprettoppgave") {
            val params = call.receive<ArenaOpprettOppgaveParams>()
            val result = oppgaveClient.opprettOppgave(params)
            call.respond(HttpStatusCode.Created, result)
        }
        
        post("/behandleKjoerelisteOgOpprettOppgave") {
            val request = call.receive<BehandleKjoerelisteOgOpprettOppgaveRequest>()
            val result = sakOgAktivitetClient.behandleKjoerelisteOgOpprettOppgave(request.journalpostId)
            call.respond(result)
        }
    }
}
