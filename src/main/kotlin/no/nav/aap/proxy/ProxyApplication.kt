package no.nav.aap.proxy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import no.nav.aap.util.AccessorUtil
import no.nav.boot.conditionals.Cluster
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableCaching
@EnableJwtTokenValidation(ignore = ["org.springdoc", "org.springframework"])
class ProxyApplication

fun main(args : Array<String>) {
    runApplication<ProxyApplication>(*args) {
        setAdditionalProfiles(*Cluster.profiler())
        AccessorUtil.init()
        applicationStartup = BufferingApplicationStartup(4096)
    }
}