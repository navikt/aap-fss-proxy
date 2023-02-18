package no.nav.aap.proxy.arena

import jakarta.xml.bind.JAXBElement
import jakarta.xml.ws.BindingProvider.*
import java.net.URI
import java.util.*
import kotlin.collections.emptyList
import kotlin.collections.filter
import kotlin.collections.filterNot
import kotlin.collections.set
import kotlin.collections.sortedByDescending
import no.nav.aap.proxy.arena.ArenaDTOs.oppgaveReq1
import no.nav.aap.proxy.arena.ArenaDTOs.sakerReq
import no.nav.aap.proxy.arena.ArenaDTOs.toLocalDateTime
import no.nav.aap.proxy.arena.generated.oppgave.BehandleArbeidOgAktivitetOppgaveV1
import no.nav.aap.proxy.arena.generated.sak.HentSaksInfoListeV2Response
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.StringExtensions.partialMask
import org.apache.cxf.binding.soap.Soap12
import org.apache.cxf.binding.soap.SoapMessage
import org.apache.cxf.endpoint.Client
import org.apache.cxf.ext.logging.LoggingInInterceptor
import org.apache.cxf.ext.logging.LoggingOutInterceptor
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.rt.security.SecurityConstants.*
import org.apache.cxf.ws.policy.PolicyBuilder
import org.apache.cxf.ws.policy.PolicyEngine
import org.apache.cxf.ws.policy.attachment.reference.RemoteReferenceResolver
import org.apache.cxf.ws.security.trust.STSClient
import org.apache.neethi.Policy
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.ws.client.core.WebServiceOperations

@Component
class ArenaSoapAdapter(@Qualifier("sak") private val sak: WebServiceOperations, val oppgave: BehandleArbeidOgAktivitetOppgaveV1, private val cfg: ArenaSoapConfig) {

    private val log = getLogger(javaClass)

    fun hentSaker(fnr: String) =
        if (cfg.enabled) {
            (sak.marshalSendAndReceive(cfg.sakerURI,sakerReq(fnr)) as JAXBElement<HentSaksInfoListeV2Response>).value
                .saksInfoListe.saksInfo
                .filter { it.sakstatus.equals(AKTIV,ignoreCase = true) }
                .filterNot { it.sakstypekode.equals(KLAGEANKE, ignoreCase = true) }
                .sortedByDescending { it.sakOpprettet.toLocalDateTime() }.also {
                    log.info("Saker for ${fnr.partialMask()} er $it")
                }
        } else {
            emptyList()
        }
    fun opprettOppgave(params: ArenaOpprettOppgaveParams) = oppgave.bestillOppgave(oppgaveReq1(params))

    companion object {
        private const val AKTIV = "Aktiv"
        private const val KLAGEANKE = "KLAN"
    }

    @Component
    class WsClient<T>(val endpointStsClientConfig: EndpointSTSClientConfig) {

        fun configureClientForSystemUser(port: T): T {
            val client = ClientProxy.getClient(port)
            // client.outInterceptors.add(CallIdHeaderInterceptor())
            val loggingInInterceptor = LoggingInInterceptor()
            loggingInInterceptor.setPrettyLogging(true)
            val loggingOutInterceptor = LoggingOutInterceptor()
            loggingOutInterceptor.setPrettyLogging(true)
            client.inInterceptors.add(loggingInInterceptor)
            client.inFaultInterceptors.add(loggingInInterceptor)
            client.outInterceptors.add(loggingOutInterceptor)
            client.outFaultInterceptors.add(loggingInInterceptor)
            endpointStsClientConfig.configureRequestSamlToken(port)
            return port
        }

    }

    @Component
    class EndpointSTSClientConfig(private val stsClient: STSClient) {
        fun <T> configureRequestSamlToken(port: T): T {
            val client = ClientProxy.getClient(port)
            client.requestContext[STS_CLIENT] = stsClient
            client.requestContext[CACHE_ISSUED_TOKEN_IN_ENDPOINT] = true
            setClientEndpointPolicy(client, policy(client, STS_REQUEST_SAML_POLICY))
            return port
        }
        private fun policy(client: Client, uri: String) = RemoteReferenceResolver("", client.bus.getExtension(PolicyBuilder::class.java)).resolveReference(uri)

        private fun setClientEndpointPolicy(client: Client, policy: Policy) {
            val endpoint = client.endpoint
            val endpointInfo = endpoint.endpointInfo
            val policyEngine = client.bus.getExtension(PolicyEngine::class.java)
            val message = SoapMessage(Soap12.getInstance())
            val endpointPolicy = policyEngine.getClientEndpointPolicy(endpointInfo, null, message)
            policyEngine.setClientEndpointPolicy(endpointInfo, endpointPolicy.updatePolicy(policy, message))
        }

        companion object {
            private const val STS_REQUEST_SAML_POLICY = "classpath:policy/requestSamlPolicy.xml"
        }
    }

    @ConfigurationProperties(prefix = "securitytokenservice")
    data class STSWSClientConfig( val url: URI,  val username: String,  val password: String, )

}