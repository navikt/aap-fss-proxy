package no.nav.aap.joark

import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URI

@ConfigurationProperties("joark")
class JoarkConfig @ConstructorBinding constructor(baseUri: URI, @DefaultValue("rest/journalpostapi/v1/journalpost") val path: String, @DefaultValue("isAlive") pingPath: String,@DefaultValue("true") enabled: Boolean): AbstractRestConfig(baseUri,pingPath,enabled)