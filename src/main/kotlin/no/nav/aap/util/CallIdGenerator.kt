package no.nav.aap.api.util

import org.springframework.stereotype.Component
import java.util.*

@Component
class CallIdGenerator {
    fun create() = UUID.randomUUID().toString()
}