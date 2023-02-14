package no.nav.aap.proxy.arena

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.proxy.arena.ArenaConfig.Companion.ARENA
import no.nav.aap.proxy.arena.ArenaOIDCConfig.Companion.ARENAOIDC
import no.nav.aap.proxy.sts.StsWebClientAdapter
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.StringExtensions.asBearer
import org.apache.wss4j.common.ConfigurationConstants.USERNAME_TOKEN
import org.apache.wss4j.common.WSS4JConstants.PW_TEXT
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.webservices.client.WebServiceTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.*
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient.Builder
import org.springframework.ws.client.support.interceptor.ClientInterceptor
import org.springframework.ws.client.support.interceptor.ClientInterceptorAdapter
import org.springframework.ws.context.MessageContext
import org.springframework.ws.soap.SoapMessage
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor
import org.springframework.ws.transport.http.HttpComponentsMessageSender

@Configuration
class ArenaBeanConfig {

    private val log = LoggerUtil.getLogger(javaClass)

    @Bean
    @Qualifier(ARENAOIDC)
    fun arenaOIDCWebClient(builder: Builder, cf: ArenaOIDCConfig, @Qualifier(ARENAOIDC) filter: ExchangeFilterFunction) =
        builder
            .baseUrl("${cf.baseUri}")
            .filter(filter)
            .build()

    @Bean
    @Qualifier(ARENAOIDC)
    fun arenaOIDCExchangeFilterFunction(cfg: ArenaUserConfig) =
        ExchangeFilterFunction {
            req, next -> next.exchange(
                ClientRequest.from(req).header(AUTHORIZATION, cfg.credentials)
                    .build())
        }

    @Bean
    @Qualifier(ARENA)
    fun arenaWebClient(builder: Builder, cfg: ArenaConfig, @Qualifier(ARENA) arenaExchangeFilterFunction: ExchangeFilterFunction) =
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
        contextPath = "no.nav.aap.proxy.arena.generated"

    }
    @Bean
    fun webServiceOperations(builder: WebServiceTemplateBuilder, marshaller: Jaxb2Marshaller,vararg interceptors: ClientInterceptor) =
        builder.messageSenders(HttpComponentsMessageSender())
            .setDefaultUri("https://arena-q1.adeo.no/arena_ws/services/ArenaSakVedtakService") // TODO
            .setMarshaller(marshaller)
            .setUnmarshaller(marshaller).build().apply {
                log.info("Eksisterende interceptors ${getInterceptors()}")
                setInterceptors(interceptors)
            //    setFaultMessageResolver { FaultMessageResolver { log.warn(("OOPS, dette feilet $it")) } }
            }

    @Bean
     fun securityInterceptor(cf: ArenaUserConfig) = Wss4jSecurityInterceptor().apply{
         setSecurementActions(USERNAME_TOKEN)
         setSecurementUsername(cf.id)
         setSecurementPassword(cf.secret)
         setSecurementPasswordType(PW_TEXT)
     }
    @Bean
    fun faultHandler() = object: ClientInterceptorAdapter() {
        override fun handleFault(ctx: MessageContext): Boolean {
            with((ctx.response as SoapMessage).envelope.body) {
                log.error("OOPS FSOR ${fault.faultStringOrReason}")
                log.error("OOPS CODE ${fault.faultCode}")
                fault.faultDetail.detailEntries.forEach { log.error("OOPS ENTRY $it") }
            }
            return true
        }}
}