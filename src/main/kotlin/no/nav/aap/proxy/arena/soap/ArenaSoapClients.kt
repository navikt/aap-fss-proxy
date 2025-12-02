package no.nav.aap.proxy.arena.soap

import no.nav.aap.proxy.arena.ArenaOpprettOppgaveParams
import no.nav.aap.proxy.arena.ArenaSakId
import no.nav.aap.proxy.arena.OpprettetOppgave
import no.nav.aap.proxy.config.AppConfig
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("ArenaSakClient")

/**
 * Client for Arena Sak SOAP service.
 * 
 * NOTE: Full SOAP implementation requires WSDL-generated stubs and CXF/SAML configuration
 * which depends on NAV-internal packages. The actual SOAP client implementation should be
 * added when deploying to an environment with access to NAV's internal Maven repositories.
 * 
 * The WSDL files are still present in src/main/resources/wsdl/ and can be used to generate
 * the required stubs.
 */
class ArenaSakClient(private val config: AppConfig) {
    private val soapConfig = config.arenasoap
    
    fun nyesteAktiveSak(fnr: String): String? {
        if (!soapConfig.enabled) {
            log.info("Arena sak service is disabled")
            return null
        }
        
        log.info("Henter nyeste aktive sak for ${fnr.take(6)}*****")
        
        // The actual implementation would:
        // 1. Create a SOAP request using the generated classes from WSDL
        // 2. Configure WS-Security (username token)
        // 3. Send the request to soapConfig.sakerUri
        // 4. Parse the response and return the saksId
        
        throw NotImplementedError(
            "SOAP client requires WSDL-generated stubs and NAV-internal dependencies. " +
            "See original ArenaSoapBeanConfig for implementation reference."
        )
    }
    
    fun ping(): Map<String, String> {
        log.info("Pinging arenaSak")
        return mapOf("status" to "OK")
    }
}

/**
 * Client for Arena Oppgave SOAP service.
 * See ArenaSakClient documentation for implementation notes.
 */
class ArenaOppgaveClient(private val config: AppConfig) {
    private val soapConfig = config.arenasoap
    
    fun opprettOppgave(params: ArenaOpprettOppgaveParams): OpprettetOppgave {
        if (!soapConfig.enabled) {
            log.info("Arena oppgave service is disabled")
            return OpprettetOppgave.EMPTY
        }
        
        log.info("Oppretter oppgave for ${params.fnr}")
        
        throw NotImplementedError(
            "SOAP client requires WSDL-generated stubs and NAV-internal dependencies. " +
            "See original ArenaSoapBeanConfig for implementation reference."
        )
    }
    
    fun ping(): Map<String, String> {
        log.info("Pinging arenaOppgave")
        return mapOf("ping" to "OK")
    }
}

/**
 * Client for BehandleSakOgAktivitet SOAP service.
 * See ArenaSakClient documentation for implementation notes.
 */
class SakOgAktivitetClient(private val config: AppConfig) {
    private val soapConfig = config.arenasoap
    
    fun behandleKjoerelisteOgOpprettOppgave(journalpostId: String): ArenaSakId {
        if (!soapConfig.enabled) {
            log.info("SakOgAktivitet service is disabled")
            return ArenaSakId.EMPTY
        }
        
        log.info("BehandleKjoerelisteOgOpprettOppgave for journalpostId: $journalpostId")
        
        throw NotImplementedError(
            "SOAP client requires WSDL-generated stubs and NAV-internal dependencies. " +
            "See original ArenaSoapBeanConfig for implementation reference."
        )
    }
    
    fun ping(): Map<String, String> {
        log.info("Pinging behandleSakOgAktivitet")
        return mapOf("ping" to "OK")
    }
}
