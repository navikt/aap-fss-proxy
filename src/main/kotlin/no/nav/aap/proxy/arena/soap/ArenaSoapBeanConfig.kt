package no.nav.aap.proxy.arena.soap

import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.proxy.arena.generated.oppgave.BehandleArbeidOgAktivitetOppgaveV1
import no.nav.aap.proxy.arena.soap.ArenaSoapConfig.ArenaSTSConfig
import no.nav.aap.proxy.arena.soap.ArenaSoapConfig.Companion.SAK
import no.nav.boot.conditionals.EnvUtil.isDevOrLocal
import org.apache.cxf.Bus
import org.apache.cxf.binding.soap.Soap12
import org.apache.cxf.binding.soap.SoapMessage
import org.apache.cxf.endpoint.Client
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

@Configuration
class ArenaSoapBeanConfig {
    @Bean
    fun arenaOppgaveClient(ws: WsClient<BehandleArbeidOgAktivitetOppgaveV1>, cfg: ArenaSoapConfig) =
        ws.configureClientForSystemUserSAML(JaxWsProxyFactoryBean().apply {
            address = cfg.oppgaveUri
            serviceClass = BehandleArbeidOgAktivitetOppgaveV1::class.java
        }.create() as BehandleArbeidOgAktivitetOppgaveV1)

    @Bean
    fun arenaStsClient(bus: Bus, cfg: ArenaSoapConfig, env: Environment) =
        STSClient(bus).apply {
            isEnableAppliesTo = false
            isAllowRenewing = false
            location = "${cfg.sts.url}"
            properties = mapOf(USERNAME to cfg.sts.username, PASSWORD to cfg.sts.password)
            setPolicy(STS_CLIENT_AUTHENTICATION_POLICY)
            addLoggingInterceptors(env)
        }
    @Bean
    fun webServiceMarshaller() = Jaxb2Marshaller().apply {
        setContextPaths("no.nav.aap.proxy.arena.generated.sak")
    }
    @Bean
    @Qualifier(SAK)
    fun arenaSakClient(builder: WebServiceTemplateBuilder, cfg: ArenaSoapConfig, marshaller: Jaxb2Marshaller) =
        builder.messageSenders(HttpComponentsMessageSender())
            .setDefaultUri(cfg.baseUri)
            .setMarshaller(marshaller)
            .setUnmarshaller(marshaller).build().apply {
                interceptors = arrayOf(arenaSakSecurityInterceptor(cfg))
                faultMessageResolver = FaultMessageResolver {
                    throw IntegrationException((it as SaajSoapMessage).faultReason)
                }
            }
    fun arenaSakSecurityInterceptor(cfg: ArenaSoapConfig) = Wss4jSecurityInterceptor().apply {
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
class WsClient<T>( private val sts: STSClient, private val env: Environment) {

    fun configureClientForSystemUserSAML(port: T): T {
        ClientProxy.getClient(port).apply {
            requestContext[STS_CLIENT] = sts
            requestContext[CACHE_ISSUED_TOKEN_IN_ENDPOINT] = true
            setClientEndpointPolicy(this, policy(this))
            addLoggingInterceptors(env)
        }
        return port
    }

    private fun setClientEndpointPolicy(client: Client, policy: Policy) {
        with(client.bus.getExtension(PolicyEngine::class.java)) {
            val message = SoapMessage(Soap12.getInstance())
            val endpointInfo = client.endpoint.endpointInfo
            setClientEndpointPolicy(endpointInfo, getClientEndpointPolicy(endpointInfo, null, message).updatePolicy(policy, message))
        }
    }

    private fun policy(client: Client) = RemoteReferenceResolver("",
            client.bus.getExtension(PolicyBuilder::class.java)).resolveReference(STS_REQUEST_SAML_POLICY)

    companion object {
        private const val STS_REQUEST_SAML_POLICY = "classpath:policy/requestSamlPolicy.xml"
    }
}
private fun InterceptorProvider.addLoggingInterceptors(env: Environment)  {
    outInterceptors.add(ArenaSoapCallIdHeaderInterceptor())
    if (isDevOrLocal(env)) {
        val inI = LoggingOutInterceptor().apply { setPrettyLogging(true) }
        val outI = LoggingOutInterceptor().apply { setPrettyLogging(true) }
        with(this) {
            inInterceptors.add(inI)
            inFaultInterceptors.add((inI))
            outInterceptors.add(outI)
            outFaultInterceptors.add(outI)
        }
    }
}