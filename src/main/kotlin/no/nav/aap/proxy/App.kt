package no.nav.aap.proxy

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.aap.proxy.config.AppConfig
import no.nav.aap.proxy.plugins.*

fun main() {
    val config = AppConfig.load()
    embeddedServer(
        Netty,
        port = config.server.port,
        host = "0.0.0.0",
        module = { module(config) }
    ).start(wait = true)
}

fun Application.module(config: AppConfig) {
    configureSerialization()
    configureCallLogging()
    configureMetrics()
    configureAuthentication(config)
    configureStatusPages()
    configureRouting(config)
}
