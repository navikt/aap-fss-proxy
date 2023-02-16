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
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import org.apache.wss4j.common.ConfigurationConstants
import org.apache.wss4j.common.ConfigurationConstants.USERNAME_TOKEN
import org.apache.wss4j.common.WSS4JConstants.PW_TEXT
import org.apache.wss4j.common.saml.SAMLCallback
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
import org.springframework.ws.client.support.interceptor.ClientInterceptor
import org.springframework.ws.client.support.interceptor.ClientInterceptorAdapter
import org.springframework.ws.context.MessageContext
import org.springframework.ws.soap.SoapMessage
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
    fun sakServiceOperations(builder: WebServiceTemplateBuilder,cfg: ArenaSoapConfig, marshaller: Jaxb2Marshaller, @Qualifier("sak") interceptor: ClientInterceptor) =
        builder.messageSenders(HttpComponentsMessageSender())
            .setDefaultUri(cfg.baseUri)
            .setMarshaller(marshaller)
            .setUnmarshaller(marshaller).build().apply {
                setInterceptors(interceptors)
                faultMessageResolver = FaultMessageResolver { msg -> msg as SaajSoapMessage
                    throw IntegrationException(msg.faultReason)
                }
            }

    @Bean
    @Qualifier("oppgave")
    fun oppgaveServiceOperations(builder: WebServiceTemplateBuilder,cfg: ArenaSoapConfig, marshaller: Jaxb2Marshaller, @Qualifier("oppgave") interceptor: ClientInterceptor) =
        builder.messageSenders(HttpComponentsMessageSender())
            .setDefaultUri(cfg.oppgaveUri)
            .setMarshaller(marshaller)
            .setUnmarshaller(marshaller).build().apply {
                setInterceptors(interceptors)
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
    fun samlSecurityInterceptor(cfg: ArenaSoapConfig) = Wss4jSecurityInterceptor().apply{
       setValidationActions("NoSecurity")
       setSecurementSamlCallbackHandler({ log.info("XXXXXXXXXXX") })
    }
}