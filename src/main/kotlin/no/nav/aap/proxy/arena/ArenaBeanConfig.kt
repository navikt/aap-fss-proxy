package no.nav.aap.proxy.arena

import javax.security.auth.callback.Callback
import javax.security.auth.callback.CallbackHandler
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.proxy.arena.ArenaRestConfig.Companion.ARENA
import no.nav.aap.proxy.arena.ArenaRestConfig.Companion.ARENAOIDC
import no.nav.aap.proxy.sts.StsWebClientAdapter
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.StringExtensions.asBearer
import org.apache.wss4j.common.ConfigurationConstants.SAML_TOKEN_UNSIGNED
import org.apache.wss4j.common.ConfigurationConstants.USERNAME_TOKEN
import org.apache.wss4j.common.WSS4JConstants.PW_TEXT
import org.apache.wss4j.common.saml.SAMLCallback
import org.apache.wss4j.common.saml.bean.SubjectBean
import org.apache.wss4j.common.saml.bean.Version
import org.apache.wss4j.common.saml.bean.Version.*
import org.apache.wss4j.common.saml.builder.SAML2Constants
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

    @Bean
    @Qualifier("oppgave")
    fun oppgaveServiceOperations(builder: WebServiceTemplateBuilder,cfg: ArenaSoapConfig, marshaller: Jaxb2Marshaller) =
        builder.messageSenders(HttpComponentsMessageSender())
            .setDefaultUri(cfg.oppgaveUri)
            .setMarshaller(marshaller)
            .setUnmarshaller(marshaller).build().apply {
                interceptors = arrayOf(oppgaveSecurityInterceptor(cfg))
                faultMessageResolver = FaultMessageResolver { msg -> msg as SaajSoapMessage
                    throw IntegrationException(msg.faultReason)
                }
            }

    @Bean
    @Qualifier("sak")
    fun sakSecurityInterceptor(cfg: ArenaSoapConfig) = Wss4jSecurityInterceptor().apply{
        setSecurementActions(USERNAME_TOKEN)
        setSecurementUsername(cfg.credentials.id)
        setSecurementPassword(cfg.credentials.secret)
        setSecurementPasswordType(PW_TEXT)
    }

    @Bean
    @Qualifier("oppgave")
    fun oppgaveSecurityInterceptor(cfg: ArenaSoapConfig) = Wss4jSecurityInterceptor().apply{
        setSecurementActions(SAML_TOKEN_UNSIGNED)
        setSecurementSamlCallbackHandler { SamlCallbackHandler() }
    }
    private class SamlCallbackHandler() : CallbackHandler {
        private val log = LoggerUtil.getLogger(javaClass)

        override fun handle(callbacks: Array<Callback>) {
            for (value in callbacks) {
                if (value is SAMLCallback) {
                    log.info("XXXXXXXXXXX $value")
                    value.setSamlVersion(SAML_20)
                    value.subject = SubjectBean("test-subject", "", null)
                }
            }
        }
    }
}