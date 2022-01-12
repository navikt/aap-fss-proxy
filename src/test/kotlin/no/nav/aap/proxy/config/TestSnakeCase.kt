package no.nav.aap.proxy.config

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.aap.proxy.sts.OidcToken
import no.nav.security.token.support.core.jwt.JwtToken
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester
import org.springframework.test.context.ContextConfiguration


@JsonTest
@ContextConfiguration(classes = arrayOf(ObjectMapper::class))
class TestSnakeCase {

    @Autowired
    private lateinit var mapper: ObjectMapper

    private val j = """
         {
           "expires_in" :1,
           "access_token" :"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJOQVYgdGVzdCIsImlhdCI6MTYzNjM2NjgxOCwiZXhwIjoxNjY3OTAyODE4LCJhdWQiOiJ3d3cubmF2Lm5vIiwic3ViIjoiMTExMTExMTExMSJ9.eTGkBsYMNKY7N9wX9xnY6jUTOSat4oxPEWU4wCeYsHQ",
           "token_type" :"b"
         }"""


    @Test
    fun serialize() {

        println(mapper.readValue(j,OidcToken::class.java))

    }
}