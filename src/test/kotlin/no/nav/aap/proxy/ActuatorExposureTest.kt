package no.nav.aap.proxy

import org.junit.jupiter.api.Test
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.test.web.reactive.server.WebTestClient
import no.nav.aap.actuatoronly.TestActuatorApplication

class ActuatorExposureTest {

    @Test
    fun `skal ikke eksponere actuator env`() {
        val context = SpringApplicationBuilder(TestActuatorApplication::class.java)
            .properties(
                mapOf(
                    "spring.config.location" to "file:src/main/resources/application.yml,file:src/main/resources/application-prod-fss.yaml",
                    "server.port" to "0"
                )
            )
            .run()

        try {
            val port = context.environment.getProperty("local.server.port", Int::class.java)!!
            WebTestClient.bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .get()
                .uri("/actuator/env")
                .exchange()
                .expectStatus()
                .isNotFound
        } finally {
            context.close()
        }
    }
}
