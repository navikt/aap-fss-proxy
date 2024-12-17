package no.nav.aap.proxy.arena.soap

import org.apache.cxf.Bus
import org.apache.cxf.binding.soap.Soap12
import org.apache.cxf.binding.soap.SoapMessage
import org.apache.cxf.endpoint.Client
import org.apache.cxf.ext.logging.LoggingInInterceptor
import org.apache.cxf.ext.logging.LoggingOutInterceptor
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.interceptor.InterceptorProvider
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
import org.springframework.core.env.Environment
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.stereotype.Component
import org.springframework.ws.client.core.FaultMessageResolver
import org.springframework.ws.soap.saaj.SaajSoapMessage
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor
import org.springframework.ws.transport.http.HttpComponentsMessageSender
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.proxy.arena.generated.behandleSakOgAktivitet.BehandleSakOgAktivitetV1
import no.nav.aap.proxy.arena.generated.oppgave.BehandleArbeidOgAktivitetOppgaveV1
import no.nav.aap.proxy.arena.soap.ArenaSoapConfig.Companion.SAK

@Configuration(proxyBeanMethods = false)
class ArenaSoapBeanConfig(private val cfg : ArenaSoapConfig) {

    @Bean
    fun arenaOppgaveHealthIndicator(a : ArenaOppgaveSoapAdapter) = object : AbstractPingableHealthIndicator(a) {}

    @Bean
    fun arenaSakHealthIndicator(a : ArenaSakSoapAdapter) = object : AbstractPingableHealthIndicator(a) {}

    @Bean
    fun arenaOppgaveClient(ws : WsClient<BehandleArbeidOgAktivitetOppgaveV1>) =
        ws.configureClientForSystemUserSAML(JaxWsProxyFactoryBean().apply {
            address = cfg.oppgaveUri
            serviceClass = BehandleArbeidOgAktivitetOppgaveV1::class.java
        }.create() as BehandleArbeidOgAktivitetOppgaveV1)

    @Bean
    fun arenaBehandleSalOgAktivitetClient(ws : WsClient<BehandleSakOgAktivitetV1>) =
        ws.configureClientForSystemUserSAML(JaxWsProxyFactoryBean().apply {
            address = cfg.behandleSakOgAktivitetUri
            serviceClass = BehandleSakOgAktivitetV1::class.java
        }.create() as BehandleSakOgAktivitetV1)

    @Bean
    fun arenaStsClient(bus : Bus, env : Environment) =
        STSClient(bus).apply {
            isEnableAppliesTo = false
            isAllowRenewing = false
            location = "${cfg.sts.url}"
            properties = mapOf(USERNAME to cfg.sts.username, PASSWORD to cfg.sts.password)
            setPolicy(STS_CLIENT_AUTHENTICATION_POLICY)
            addLoggingInterceptors()
        }

    @Bean
    fun webServiceMarshaller() = Jaxb2Marshaller().apply {
        setContextPaths("no.nav.aap.proxy.arena.generated.sak")
    }

    @Bean
    @Qualifier(SAK)
    fun arenaSakClient(builder : WebServiceTemplateBuilder, marshaller : Jaxb2Marshaller) =
        builder.messageSenders(HttpComponentsMessageSender())
            .setDefaultUri(cfg.baseUri)
            .setMarshaller(marshaller)
            .setUnmarshaller(marshaller).build().apply {
                interceptors = arrayOf(arenaSakSecurityInterceptor())
                faultMessageResolver = FaultMessageResolver {
                    throw IrrecoverableIntegrationException((it as SaajSoapMessage).faultReason)
                }
            }

    fun arenaSakSecurityInterceptor() = Wss4jSecurityInterceptor().apply {
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
class WsClient<T>(private val sts : STSClient) {

    fun configureClientForSystemUserSAML(port : T) : T {
        ClientProxy.getClient(port).apply {
            requestContext[STS_CLIENT] = sts
            requestContext[CACHE_ISSUED_TOKEN_IN_ENDPOINT] = true
            setClientEndpointPolicy(this, policy(this))
            addLoggingInterceptors()
        }
        return port
    }

    private fun setClientEndpointPolicy(client : Client, policy : Policy) {
        with(client.bus.getExtension(PolicyEngine::class.java)) {
            val message = SoapMessage(Soap12.getInstance())
            val endpointInfo = client.endpoint.endpointInfo
            setClientEndpointPolicy(endpointInfo, getClientEndpointPolicy(endpointInfo, null, message).updatePolicy(policy, message))
        }
    }

    private fun policy(client : Client) = RemoteReferenceResolver("",
        client.bus.getExtension(PolicyBuilder::class.java)).resolveReference(STS_REQUEST_SAML_POLICY)

    companion object {

        private const val STS_REQUEST_SAML_POLICY = "classpath:policy/requestSamlPolicy.xml"
    }
}

private fun InterceptorProvider.addLoggingInterceptors() {
    outInterceptors.add(ArenaSoapCallIdHeaderInterceptor())
    val inI = LoggingInInterceptor().apply { setPrettyLogging(true) }
    val outI = LoggingOutInterceptor().apply { setPrettyLogging(true) }
    with(this) {
        inInterceptors.add(inI)
        inFaultInterceptors.add((inI))
        outInterceptors.add(outI)
        outFaultInterceptors.add(outI)
    }
}