package no.nav.aap.proxy.arena

import org.springframework.boot.context.properties.bind.DefaultValue

data class ArenaCredentials( @DefaultValue("id") val id: String,  @DefaultValue("secret") val secret: String)