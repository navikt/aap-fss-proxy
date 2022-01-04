package no.nav.aap.proxy

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.retry.annotation.EnableRetry
import no.nav.boot.conditionals.Cluster.profiler
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableRetry
@EnableJwtTokenValidation(ignore = ["org.springdoc", "org.springframework"])

class ProxyApplication
fun main(args: Array<String>) {
	runApplication<ProxyApplication>(*args) {
		setAdditionalProfiles(*profiler())
		applicationStartup = BufferingApplicationStartup(4096)
	}
}