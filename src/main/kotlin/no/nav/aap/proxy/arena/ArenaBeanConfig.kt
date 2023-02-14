package no.nav.aap.proxy.arena

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.proxy.arena.ArenaConfig.Companion.ARENA
import no.nav.aap.proxy.arena.ArenaOIDCConfig.Companion.ARENAOIDC
import no.nav.aap.proxy.sts.StsWebClientAdapter
import no.nav.aap.util.StringExtensions.asBearer
import org.apache.wss4j.dom.WSConstants.PW_TEXT
import org.apache.wss4j.dom.handler.WSHandlerConstants.USERNAME_TOKEN
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.webservices.client.WebServiceTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.*
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient.Builder
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor
import org.springframework.ws.transport.http.HttpComponentsMessageSender

@Configuration
class ArenaBeanConfig {
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
                ClientRequest.from(req).header(AUTHORIZATION, "Basic ${cfg.credentials}")
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
    fun webServiceOperations(builder: WebServiceTemplateBuilder, marshaller: Jaxb2Marshaller,interceptor: Wss4jSecurityInterceptor) =
        builder.messageSenders(HttpComponentsMessageSender())
            .setDefaultUri("https://arena-q1.adeo.no/arena_ws/services/ArenaSakVedtakService") // TODO
            .setMarshaller(marshaller)
            .setUnmarshaller(marshaller).build().apply {
                interceptors = arrayOf(interceptor)
            }

    @Bean
     fun securityInterceptor(cf: ArenaUserConfig) = Wss4jSecurityInterceptor().apply {
         setSecurementActions(USERNAME_TOKEN)
         setSecurementUsername(cf.id)
         setSecurementPassword(cf.secret)
         setSecurementPasswordType(PW_TEXT)
     }
}