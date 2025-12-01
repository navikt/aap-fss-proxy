package no.nav.aap.util

import java.util.UUID

object CallIdGenerator {

    fun create() = "${UUID.randomUUID()}"
}