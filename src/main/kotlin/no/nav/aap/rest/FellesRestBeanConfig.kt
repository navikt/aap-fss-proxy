package no.nav.aap.rest

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.zalando.problem.jackson.ProblemModule


@Configuration
class FellesRestBeanConfig {
@Bean
 fun customizer(): Jackson2ObjectMapperBuilderCustomizer {
    return Jackson2ObjectMapperBuilderCustomizer { b: Jackson2ObjectMapperBuilder ->
        b.modules(ProblemModule(), JavaTimeModule(), KotlinModule())
    }
  }

    @Bean
    fun springShopOpenAPI(): OpenAPI? {
        return OpenAPI()
            .info(
                Info().title("SpringShop API")
                    .description("Spring shop sample application")
                    .version("v0.0.1")
                    .license(License().name("Apache 2.0").url("http://springdoc.org"))
            )
            .externalDocs(
                ExternalDocumentation()
                    .description("SpringShop Wiki Documentation")
                    .url("https://springshop.wiki.github.org/docs")
            )
    }
}