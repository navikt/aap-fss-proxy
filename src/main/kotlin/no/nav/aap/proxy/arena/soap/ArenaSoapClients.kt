package no.nav.aap.proxy.arena.soap

import no.nav.aap.proxy.arena.ArenaOpprettOppgaveParams
import no.nav.aap.proxy.arena.ArenaSakId
import no.nav.aap.proxy.arena.OpprettetOppgave
import no.nav.aap.proxy.config.AppConfig
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("ArenaSakClient")

/**
 * Client for Arena Sak SOAP service.
 * Note: Full SOAP implementation requires WSDL-generated stubs.
 * This is a placeholder that would integrate with the actual SOAP service.
 */
class ArenaSakClient(private val config: AppConfig) {
    private val soapConfig = config.arenasoap
    
    fun nyesteAktiveSak(fnr: String): String? {
        if (!soapConfig.enabled) {
            log.info("Arena sak service is disabled")
            return null
        }
        
        log.info("Henter nyeste aktive sak for ${fnr.take(6)}*****")
        
        // TODO: Implement actual SOAP call using CXF
        // This would use the generated JAXWS stubs from the WSDL files
        // For now, this is a placeholder that would be replaced with actual implementation
        
        // The actual implementation would:
        // 1. Create a SOAP request using the generated classes
        // 2. Configure WS-Security (username token)
        // 3. Send the request to soapConfig.sakerUri
        // 4. Parse the response and return the saksId
        
        throw NotImplementedError("SOAP client requires WSDL-generated stubs. See ArenaSoapBeanConfig in original code.")
    }
    
    fun ping(): Map<String, String> {
        log.info("Pinger arenaSak")
        // Would call nyesteAktiveSak with test FNR
        return mapOf("status" to "OK")
    }
}

/**
 * Client for Arena Oppgave SOAP service.
 */
class ArenaOppgaveClient(private val config: AppConfig) {
    private val soapConfig = config.arenasoap
    
    fun opprettOppgave(params: ArenaOpprettOppgaveParams): OpprettetOppgave {
        if (!soapConfig.enabled) {
            log.info("Arena oppgave service is disabled")
            return OpprettetOppgave.EMPTY
        }
        
        log.info("Oppretter oppgave for ${params.fnr}")
        
        // TODO: Implement actual SOAP call using CXF
        // This would use the generated JAXWS stubs from the WSDL files
        
        throw NotImplementedError("SOAP client requires WSDL-generated stubs. See ArenaSoapBeanConfig in original code.")
    }
    
    fun ping(): Map<String, String> {
        log.info("Pinger arenaOppgave")
        return mapOf("ping" to "OK")
    }
}

/**
 * Client for BehandleSakOgAktivitet SOAP service.
 */
class SakOgAktivitetClient(private val config: AppConfig) {
    private val soapConfig = config.arenasoap
    
    fun behandleKjoerelisteOgOpprettOppgave(journalpostId: String): ArenaSakId {
        if (!soapConfig.enabled) {
            log.info("SakOgAktivitet service is disabled")
            return ArenaSakId.EMPTY
        }
        
        log.info("BehandleKjoerelisteOgOpprettOppgave for journalpostId: $journalpostId")
        
        // TODO: Implement actual SOAP call using CXF
        
        throw NotImplementedError("SOAP client requires WSDL-generated stubs. See ArenaSoapBeanConfig in original code.")
    }
    
    fun ping(): Map<String, String> {
        log.info("ping til behandleSakOgAktivtet")
        return mapOf("ping" to "OK")
    }
}
