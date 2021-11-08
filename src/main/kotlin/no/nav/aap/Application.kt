package no.nav.aap

import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.retry.annotation.EnableRetry
import no.nav.boot.conditionals.Cluster.profiler

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableRetry
@EnableJwtTokenValidation(ignore = ["springfox.documentation", "org.springframework"])
@EnableOAuth2Client(cacheEnabled = true)
class Application

fun main(args: Array<String>) {
	SpringApplicationBuilder(Application::class.java)
		.profiles(*profiler())
		.main(Application::class.java)
		.run(*args)
}