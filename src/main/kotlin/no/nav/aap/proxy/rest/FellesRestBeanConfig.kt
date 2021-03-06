package no.nav.aap.proxy.rest

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.netty.handler.logging.LogLevel.TRACE
import no.nav.aap.rest.HeadersToMDCFilter
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import no.nav.aap.proxy.sts.StsClient
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.aap.rest.ActuatorIgnoringTraceRequestFilter
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants.STS
import no.nav.aap.util.StartupInfoContributor
import no.nav.aap.util.StringExtensions.asBearer
import no.nav.boot.conditionals.ConditionalOnDevOrLocal
import no.nav.boot.conditionals.ConditionalOnNotProd
import no.nav.boot.conditionals.EnvUtil.isDevOrLocal
import no.nav.security.token.support.client.spring.oauth2.ClientConfigurationPropertiesMatcher
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.trace.http.HttpExchangeTracer
import org.springframework.boot.actuate.trace.http.HttpTraceRepository
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders.*
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.zalando.problem.jackson.ProblemModule
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat.TEXTUAL

@Configuration
class FellesRestBeanConfig(@Value("\${spring.application.name}") val applicationName: String) {
@Bean
 fun jacksonCustomizer()  =
     Jackson2ObjectMapperBuilderCustomizer {
         b: Jackson2ObjectMapperBuilder -> b.modules(ProblemModule(), JavaTimeModule(), KotlinModule.Builder().build())
    }

    @Bean
    fun swagger(p: BuildProperties): OpenAPI {
        return OpenAPI()
            .info(Info()
                .title("AAP fss proxy")
                    .description("Proxy mot tjenester som ikke st??tter AAD/TokenX")
                    .version(p.version)
                    .license(License()
                        .name("MIT")
                        .url("https://nav.no"))
            )
    }

    @Bean
    @ConditionalOnDevOrLocal
    fun httpTraceRepository() = InMemoryHttpTraceRepository()

    @Bean
    @ConditionalOnDevOrLocal
    fun actuatorIgnoringTraceRequestFilter(repo: HttpTraceRepository, tracer: HttpExchangeTracer) =
        ActuatorIgnoringTraceRequestFilter(repo, tracer)
    @Bean
    fun startupInfoContributor(ctx: ApplicationContext) = StartupInfoContributor(ctx)

    @Bean
    fun authContext(ctxHolder: TokenValidationContextHolder) = AuthContext(ctxHolder)

    @Bean
    fun configMatcher() = object : ClientConfigurationPropertiesMatcher {}

    @Bean
    @Qualifier(STS)
    fun stsExchangeFilterFunction(stsClient: StsClient) =
        ExchangeFilterFunction {
            req, next -> next.exchange(ClientRequest.from(req).header(AUTHORIZATION, "${stsClient.oidcToken().asBearer()}")
            .build())
        }

    @Bean
    fun headersToMDCFilterRegistrationBean() =
        FilterRegistrationBean(HeadersToMDCFilter(applicationName))
            .apply {
                urlPatterns = listOf("/*")
                setOrder(HIGHEST_PRECEDENCE)
            }

    @Bean
    fun webClientCustomizer(env: Environment) =
        WebClientCustomizer { b ->
            b.clientConnector(ReactorClientHttpConnector(client(env)))
                .filter(correlatingFilterFunction(applicationName))
        }

    private fun client(env: Environment) =
        if (isDevOrLocal(env))
            HttpClient.create().wiretap(javaClass.canonicalName, TRACE, TEXTUAL)
        else HttpClient.create()
}