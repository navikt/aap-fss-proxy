package no.nav.aap.proxy

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.retry.annotation.EnableRetry
import no.nav.boot.conditionals.Cluster.profiler
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableRetry
@EnableOAuth2Client
@EnableJwtTokenValidation(ignore = ["org.springdoc", "org.springframework"])

class ProxyApplication
fun main(args: Array<String>) {
	runApplication<ProxyApplication>(*args) {
		setAdditionalProfiles(*profiler())
		applicationStartup = BufferingApplicationStartup(4096)
	}
}