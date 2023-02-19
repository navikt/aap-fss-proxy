package no.nav.aap.proxy.arena.soap

import java.util.Map
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.proxy.arena.generated.oppgave.BehandleArbeidOgAktivitetOppgaveV1
import no.nav.aap.proxy.arena.soap.ArenaSoapAdapter.STSWSClientConfig
import no.nav.aap.proxy.arena.soap.ArenaSoapAdapter.WsClient
import no.nav.aap.proxy.arena.soap.ArenaSoapConfig.Companion.SAK
import org.apache.cxf.Bus
import org.apache.cxf.ext.logging.LoggingInInterceptor
import org.apache.cxf.ext.logging.LoggingOutInterceptor
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.rt.security.SecurityConstants
import org.apache.cxf.ws.security.trust.STSClient
import org.apache.wss4j.common.ConfigurationConstants
import org.apache.wss4j.common.WSS4JConstants
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.webservices.client.WebServiceTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.ws.client.core.FaultMessageResolver
import org.springframework.ws.soap.saaj.SaajSoapMessage
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor
import org.springframework.ws.transport.http.HttpComponentsMessageSender

@Configuration
class ArenaSoapBeanConfig {
    @Bean
    fun oppgaveClient(ws: WsClient<BehandleArbeidOgAktivitetOppgaveV1>) =
        ws.configureClientForSystemUser(JaxWsProxyFactoryBean().apply {
            address = "https://arena-q1.adeo.no/ail_ws/BehandleArbeidOgAktivitetOppgave_v1"
            serviceClass = BehandleArbeidOgAktivitetOppgaveV1::class.java
        }.create() as BehandleArbeidOgAktivitetOppgaveV1)

    @Bean
    fun arenaStsClient(bus: Bus, cfg: STSWSClientConfig, loggingIn: LoggingInInterceptor, loggingOut: LoggingOutInterceptor) = STSClient(bus).apply {
        isEnableAppliesTo = false
        isAllowRenewing = false
        location = "${cfg.url}"
        properties = Map.of<String, Any>(SecurityConstants.USERNAME, cfg.username,
                SecurityConstants.PASSWORD, cfg.password)
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
                faultMessageResolver = FaultMessageResolver { msg -> msg as SaajSoapMessage
                    throw IntegrationException(msg.faultReason)
                }
            }


    @Bean
    fun loggingInInterceptor() = LoggingInInterceptor().apply { setPrettyLogging(true) }

    @Bean
    fun loggingOutInterceptor() = LoggingOutInterceptor().apply { setPrettyLogging(true) }


    fun sakSecurityInterceptor(cfg: ArenaSoapConfig) = Wss4jSecurityInterceptor().apply {
        setSecurementActions(ConfigurationConstants.USERNAME_TOKEN)
        with(cfg.credentials) {
            setSecurementUsername(id)
            setSecurementPassword(secret)
        }
        setSecurementPasswordType(WSS4JConstants.PW_TEXT)
    }

    companion object {
        const val STS_CLIENT_AUTHENTICATION_POLICY = "classpath:policy/untPolicy.xml"
    }
}