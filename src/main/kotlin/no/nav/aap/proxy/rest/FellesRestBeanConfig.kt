package no.nav.aap.proxy.rest

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.aap.rest.HeadersToMDCFilter
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import no.nav.aap.proxy.sts.StsClient
import no.nav.aap.rest.ActuatorIgnoringTraceRequestFilter
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.util.AuthContext
import no.nav.aap.util.StartupInfoContributor
import no.nav.aap.util.StringExtensions.asBearer
import no.nav.boot.conditionals.ConditionalOnDevOrLocal
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.ClientConfigurationPropertiesMatcher
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.trace.http.HttpExchangeTracer
import org.springframework.boot.actuate.trace.http.HttpTraceRepository
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered.LOWEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.zalando.problem.jackson.ProblemModule


@Configuration
class FellesRestBeanConfig {
@Bean
 fun customizer(): Jackson2ObjectMapperBuilderCustomizer {
    return Jackson2ObjectMapperBuilderCustomizer { b: Jackson2ObjectMapperBuilder ->
        b.modules(ProblemModule(), JavaTimeModule(), KotlinModule.Builder().build())
    }
  }

    @Bean
    fun awagger(p: BuildProperties): OpenAPI {
        return OpenAPI()
            .info(
                Info().title("AAP fss proxy")
                    .description("Proxy mot tjenester som ikke støtter AAD/TokenX")
                    .version(p.version)
                    .license(License().name("MIT").url("http://nav.no"))
            )
    }

    @Bean
    @ConditionalOnDevOrLocal
    fun httpTraceRepository(): HttpTraceRepository = InMemoryHttpTraceRepository()

    @ConditionalOnDevOrLocal
    @Bean
    fun actuatorIgnoringTraceRequestFilter(repository: HttpTraceRepository, tracer: HttpExchangeTracer) = ActuatorIgnoringTraceRequestFilter(repository,tracer)
    
    @Bean
    fun startupInfoContributor(ctx: ApplicationContext) = StartupInfoContributor(ctx)

    @Bean
    fun authContext(ctxHolder: TokenValidationContextHolder) = AuthContext(ctxHolder)

    @Bean
    fun configMatcher() = object : ClientConfigurationPropertiesMatcher {}

    @Bean
    fun stsExchangeFilterFunction(stsClient: StsClient) =
        ExchangeFilterFunction { req, next -> next.exchange(ClientRequest.from(req).header(HttpHeaders.AUTHORIZATION, "${stsClient.oidcToken().asBearer()}").build()) }


    @Component
    @Order(LOWEST_PRECEDENCE)
    class HeadersToMDCFilterRegistrationBean(@Value("\${spring.application.name}") applicationName: String) :
        FilterRegistrationBean<HeadersToMDCFilter?>(HeadersToMDCFilter(applicationName)) {
        init {
            urlPatterns = listOf("/*")
        }
    }
}