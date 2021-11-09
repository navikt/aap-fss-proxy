package no.nav.aap

import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.retry.annotation.EnableRetry
import no.nav.boot.conditionals.Cluster.profiler
import no.nav.boot.conditionals.ConditionalOnNotProd
import org.springframework.boot.actuate.trace.http.HttpTraceRepository
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository
import org.springframework.context.annotation.Bean

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableRetry
@EnableJwtTokenValidation(ignore = ["springfox.documentation", "org.springframework"])
@EnableOAuth2Client(cacheEnabled = true)
class ProxyApplication

fun main(args: Array<String>) {
	SpringApplicationBuilder(ProxyApplication::class.java)
		.profiles(*profiler())
		.main(ProxyApplication::class.java)
		.run(*args)
}

@Bean
@ConditionalOnNotProd
fun httpTraceRepository(): HttpTraceRepository {
	return InMemoryHttpTraceRepository()
}