package no.nav.aap.proxy.rest

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.aap.rest.HeadersToMDCFilter
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import no.nav.aap.proxy.sts.StsClient
import no.nav.aap.rest.ActuatorIgnoringTraceRequestFilter
import no.nav.aap.util.AuthContext
import no.nav.aap.util.StartupInfoContributor
import no.nav.aap.util.StringExtensions.asBearer
import no.nav.boot.conditionals.ConditionalOnDevOrLocal
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
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.zalando.problem.jackson.ProblemModule


@Configuration
class FellesRestBeanConfig {
@Bean
 fun customizer(): Jackson2ObjectMapperBuilderCustomizer = Jackson2ObjectMapperBuilderCustomizer {
    b  -> b.modules(ProblemModule(), JavaTimeModule(), KotlinModule.Builder().build())
  }

    @Bean
    fun awagger(p: BuildProperties): OpenAPI {
        return OpenAPI()
            .info(Info()
                .title("AAP fss proxy")
                    .description("Proxy mot tjenester som ikke støtter AAD/TokenX")
                    .version(p.version)
                    .license(License()
                        .name("MIT")
                        .url("https://nav.no"))
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
    fun stsExchangeFilterFunction(sts: StsClient) =
        ExchangeFilterFunction {
            req, next -> next.exchange(ClientRequest.from(req).header(AUTHORIZATION, "${sts.oidcToken().asBearer()}")
            .build())
        }
    @Bean
    fun headersToMDCFilterRegistrationBean(@Value("\${spring.application.name}") applicationName: String) =
        FilterRegistrationBean(HeadersToMDCFilter(applicationName)).apply {
            urlPatterns = listOf("/*")
            setOrder(HIGHEST_PRECEDENCE)
    }
}