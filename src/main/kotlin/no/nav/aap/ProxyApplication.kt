package no.nav.aap

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.retry.annotation.EnableRetry
import no.nav.boot.conditionals.Cluster.profiler

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableRetry
@EnableJwtTokenValidation(ignore = ["org.springdoc", "org.springframework"])
class ProxyApplication
fun main(args: Array<String>) {
	SpringApplicationBuilder(ProxyApplication::class.java)
		.profiles(*profiler())
		.main(ProxyApplication::class.java)
		.run(*args)
}