package no.nav.aap

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableRetry
class Application

fun main(args: Array<String>) {
	runApplication<Application>(*args)
}
