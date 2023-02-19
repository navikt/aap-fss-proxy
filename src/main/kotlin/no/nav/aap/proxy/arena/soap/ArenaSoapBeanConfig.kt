package no.nav.aap.proxy.arena.soap

import java.util.Map
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.proxy.arena.generated.oppgave.BehandleArbeidOgAktivitetOppgaveV1
import no.nav.aap.proxy.arena.soap.ArenaSoapConfig.ArenaSTSConfig
import no.nav.aap.proxy.arena.soap.ArenaSoapConfig.Companion.SAK
import org.apache.cxf.Bus
import org.apache.cxf.binding.soap.Soap12
import org.apache.cxf.binding.soap.SoapMessage
import org.apache.cxf.endpoint.Client
import org.apache.cxf.ext.logging.LoggingInInterceptor
import org.apache.cxf.ext.logging.LoggingOutInterceptor
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.rt.security.SecurityConstants.*
import org.apache.cxf.ws.policy.PolicyBuilder
import org.apache.cxf.ws.policy.PolicyEngine
import org.apache.cxf.ws.policy.attachment.reference.RemoteReferenceResolver
import org.apache.cxf.ws.security.trust.STSClient
import org.apache.neethi.Policy
import org.apache.wss4j.common.ConfigurationConstants.*
import org.apache.wss4j.common.WSS4JConstants.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.webservices.client.WebServiceTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.stereotype.Component
import org.springframework.ws.client.core.FaultMessageResolver
import org.springframework.ws.soap.saaj.SaajSoapMessage
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor
import org.springframework.ws.transport.http.HttpComponentsMessageSender

@Configuration
class ArenaSoapBeanConfig {
    @Bean
    fun oppgaveClient(ws: WsClient<BehandleArbeidOgAktivitetOppgaveV1>) =
        ws.configureClientForSystemUser(JaxWsProxyFactoryBean().apply {
            address = "https://arena-q1.adeo.no/ail_ws/BehandleArbeidOgAktivitetOppgave_v1" // TODO
            serviceClass = BehandleArbeidOgAktivitetOppgaveV1::class.java
        }.create() as BehandleArbeidOgAktivitetOppgaveV1)

    @Bean
    fun arenaStsClient(bus: Bus, cfg: ArenaSTSConfig, loggingIn: LoggingInInterceptor, loggingOut: LoggingOutInterceptor) = STSClient(bus).apply {
        isEnableAppliesTo = false
        isAllowRenewing = false
        location = "${cfg.url}"
        properties = Map.of<String, Any>(USERNAME, cfg.username, PASSWORD, cfg.password)
        setPolicy(STS_CLIENT_AUTHENTICATION_POLICY)
        with(loggingIn) {
            inInterceptors.add(this)
            inFaultInterceptors.add(this)
        }
        with(loggingOut) {
            outFaultInterceptors.add(this)
            outInterceptors.add(this)
        }
        outInterceptors.add(ArenaSoapCallIdHeaderInterceptor())
    }
    @Bean
    fun webServiceMarshaller() = Jaxb2Marshaller().apply {
        setContextPaths("no.nav.aap.proxy.arena.generated.sak","no.nav.aap.proxy.arena.generated.oppgave")
    }
    @Bean
    @Qualifier(SAK)
    fun sakServiceOperations(builder: WebServiceTemplateBuilder, cfg: ArenaSoapConfig, marshaller: Jaxb2Marshaller) =
        builder.messageSenders(HttpComponentsMessageSender())
            .setDefaultUri(cfg.baseUri)
            .setMarshaller(marshaller)
            .setUnmarshaller(marshaller).build().apply {
                interceptors = arrayOf(sakSecurityInterceptor(cfg))
                faultMessageResolver = FaultMessageResolver {
                    throw IntegrationException((it as SaajSoapMessage).faultReason)
                }
            }


    @Bean
    fun loggingInInterceptor() = LoggingInInterceptor().apply { setPrettyLogging(true) }

    @Bean
    fun loggingOutInterceptor() = LoggingOutInterceptor().apply { setPrettyLogging(true) }


    fun sakSecurityInterceptor(cfg: ArenaSoapConfig) = Wss4jSecurityInterceptor().apply {
        setSecurementActions(USERNAME_TOKEN)
        with(cfg.credentials) {
            setSecurementUsername(id)
            setSecurementPassword(secret)
        }
        setSecurementPasswordType(PW_TEXT)
    }

    companion object {
        private const val STS_CLIENT_AUTHENTICATION_POLICY = "classpath:policy/untPolicy.xml"
    }
}
@Component
class WsClient<T>( private val sts: STSClient, private val loggingIn: LoggingInInterceptor, private val loggingOut: LoggingOutInterceptor) {

    fun configureClientForSystemUser(port: T): T {
        ClientProxy.getClient(port).apply {
            outInterceptors.add(ArenaSoapCallIdHeaderInterceptor())
            with(loggingIn)  {
                inInterceptors.add(this)
                inFaultInterceptors.add(this)
            }
            with(loggingOut) {
                outInterceptors.add(this)
                outFaultInterceptors.add(this)
            }
        }
        return configureRequestSamlToken(port)
    }
    fun <T> configureRequestSamlToken(port: T): T {
        ClientProxy.getClient(port).apply {
            requestContext[STS_CLIENT] = sts
            requestContext[CACHE_ISSUED_TOKEN_IN_ENDPOINT] = true
            setClientEndpointPolicy(this, policy(this))
        }
        return port
    }
    private fun policy(client: Client) = RemoteReferenceResolver("",
            client.bus.getExtension(PolicyBuilder::class.java)).resolveReference(STS_REQUEST_SAML_POLICY)

    private fun setClientEndpointPolicy(client: Client, policy: Policy) {
        with(client.bus.getExtension(PolicyEngine::class.java)) {
            val message = SoapMessage(Soap12.getInstance())
            val endpointInfo = client.endpoint.endpointInfo
            setClientEndpointPolicy(endpointInfo, getClientEndpointPolicy(endpointInfo, null, message).updatePolicy(policy, message))
        }
}

    companion object {
        private const val STS_REQUEST_SAML_POLICY = "classpath:policy/requestSamlPolicy.xml"
    }
}