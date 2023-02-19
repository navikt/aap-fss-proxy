package no.nav.aap.proxy.arena

import jakarta.xml.bind.JAXBException
import java.util.Map
import javax.xml.namespace.QName
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.proxy.arena.ArenaRestConfig.Companion.ARENA
import no.nav.aap.proxy.arena.ArenaRestConfig.Companion.ARENAOIDC
import no.nav.aap.proxy.arena.ArenaSoapAdapter.STSWSClientConfig
import no.nav.aap.proxy.arena.ArenaSoapAdapter.WsClient
import no.nav.aap.proxy.arena.generated.oppgave.BehandleArbeidOgAktivitetOppgaveV1
import no.nav.aap.proxy.sts.StsWebClientAdapter
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.MDCUtil
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.StringExtensions.asBearer
import org.apache.cxf.Bus
import org.apache.cxf.binding.soap.SoapHeader
import org.apache.cxf.binding.soap.SoapMessage
import org.apache.cxf.ext.logging.LoggingInInterceptor
import org.apache.cxf.ext.logging.LoggingOutInterceptor
import org.apache.cxf.interceptor.Fault
import org.apache.cxf.jaxb.JAXBDataBinding
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.message.Message
import org.apache.cxf.phase.AbstractPhaseInterceptor
import org.apache.cxf.phase.Phase
import org.apache.cxf.phase.Phase.*
import org.apache.cxf.rt.security.SecurityConstants
import org.apache.cxf.ws.security.trust.STSClient
import org.apache.wss4j.common.ConfigurationConstants.USERNAME_TOKEN
import org.apache.wss4j.common.WSS4JConstants.PW_TEXT
import org.apache.wss4j.common.saml.bean.Version.*
import org.bouncycastle.crypto.tls.ConnectionEnd.client
import org.jboss.logging.MDC
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.webservices.client.WebServiceTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.*
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient.Builder
import org.springframework.ws.client.core.FaultMessageResolver
import org.springframework.ws.soap.saaj.SaajSoapMessage
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor
import org.springframework.ws.transport.http.HttpComponentsMessageSender

@Configuration
class ArenaBeanConfig {

    private val log = LoggerUtil.getLogger(javaClass)

    companion object {
        const val STS_CLIENT_AUTHENTICATION_POLICY = "classpath:policy/untPolicy.xml"
    }

    @Bean
    fun oppgaveClient(ws: WsClient<BehandleArbeidOgAktivitetOppgaveV1>): BehandleArbeidOgAktivitetOppgaveV1 {
        val jaxWsProxyFactoryBean = JaxWsProxyFactoryBean()
        jaxWsProxyFactoryBean.address ="https://arena-q1.adeo.no/ail_ws/BehandleArbeidOgAktivitetOppgave_v1"
        jaxWsProxyFactoryBean.serviceClass = BehandleArbeidOgAktivitetOppgaveV1::class.java
        val port: BehandleArbeidOgAktivitetOppgaveV1 = jaxWsProxyFactoryBean.create() as BehandleArbeidOgAktivitetOppgaveV1
        return ws.configureClientForSystemUser(port)
    }
    @Bean
    fun arenaStsClient(bus: Bus, cfg: STSWSClientConfig): STSClient {
        val sts = STSClient(bus)
        sts.isEnableAppliesTo = false
        sts.isAllowRenewing = false
        sts.location = cfg.url.toString()
        sts.properties =
            Map.of<String, Any>(SecurityConstants.USERNAME, cfg.username, SecurityConstants.PASSWORD, cfg.password)
        sts.setPolicy(STS_CLIENT_AUTHENTICATION_POLICY)
        val loggingInInterceptor = LoggingInInterceptor()
        loggingInInterceptor.setPrettyLogging(true)
        val loggingOutInterceptor = LoggingOutInterceptor()
        loggingOutInterceptor.setPrettyLogging(true)
        sts.inFaultInterceptors.add(loggingInInterceptor)
        sts.inInterceptors.add(loggingInInterceptor)
        sts.outFaultInterceptors.add(loggingOutInterceptor)
        sts.outInterceptors.add(loggingOutInterceptor)
        sts.outInterceptors.add(CallIdHeaderInterceptor())

        return sts
    }

    @Bean
    @Qualifier(ARENAOIDC)
    fun arenaOIDCWebClient(builder: Builder, cfg: ArenaRestConfig, @Qualifier(ARENAOIDC) filter: ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(filter)
            .build()

    @Bean
    @Qualifier(ARENAOIDC)
    fun arenaOIDCExchangeFilterFunction(cfg: ArenaRestConfig) =
        ExchangeFilterFunction {
            req, next -> next.exchange(
                ClientRequest.from(req).header(AUTHORIZATION, cfg.asBasic)
                    .build())
        }

    @Bean
    @Qualifier(ARENA)
    fun arenaWebClient(builder: Builder, cfg: ArenaRestConfig, @Qualifier(ARENA) arenaExchangeFilterFunction: ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(arenaExchangeFilterFunction)
            .build()

    @Bean
    @Qualifier(ARENA)
    fun arenaExchangeFilterFunction(a: ArenaOIDCWebClientAdapter) =
        ExchangeFilterFunction {
            req, next -> next.exchange(ClientRequest.from(req).header(AUTHORIZATION, a.oidcToken().asBearer())
            .build())
        }

    @Bean
    fun arenaHealthIndicator(a: StsWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}

    @Bean
    fun webServiceMarshaller() = Jaxb2Marshaller().apply {
        setContextPaths("no.nav.aap.proxy.arena.generated.sak","no.nav.aap.proxy.arena.generated.oppgave")
    }
    @Bean
    @Qualifier("sak")
    fun sakServiceOperations(builder: WebServiceTemplateBuilder,cfg: ArenaSoapConfig, marshaller: Jaxb2Marshaller) =
        builder.messageSenders(HttpComponentsMessageSender())
            .setDefaultUri(cfg.baseUri)
            .setMarshaller(marshaller)
            .setUnmarshaller(marshaller).build().apply {
                interceptors = arrayOf(sakSecurityInterceptor(cfg))
                faultMessageResolver = FaultMessageResolver { msg -> msg as SaajSoapMessage
                    throw IntegrationException(msg.faultReason)
                }
            }

    fun sakSecurityInterceptor(cfg: ArenaSoapConfig) = Wss4jSecurityInterceptor().apply{
        setSecurementActions(USERNAME_TOKEN)
        setSecurementUsername(cfg.credentials.id)
        setSecurementPassword(cfg.credentials.secret)
        setSecurementPasswordType(PW_TEXT)
    }

    class CallIdHeaderInterceptor : AbstractPhaseInterceptor<Message>(PRE_STREAM) {
        override fun handleMessage(message: Message) {
            try {
                val qName = QName("uri:no.nav.applikasjonsrammeverk", "callId")
                val header = SoapHeader(qName, MDCUtil.callId(), JAXBDataBinding(String::class.java))
                (message as SoapMessage).headers.add(header)
            }
            catch (ex: JAXBException) {
                logger.warn("Error while setting CallId header", ex)
            }
        }

        companion object {
            private val logger = LoggerFactory.getLogger(CallIdHeaderInterceptor::class.java)
        }
    }
}