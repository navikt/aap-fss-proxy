package no.nav.aap.proxy.joark

import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.util.Constants.JOARK
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URI

@ConfigurationProperties(JOARK)
@ConstructorBinding
class JoarkConfig (baseUri: URI,
                   @DefaultValue("rest/journalpostapi/v1/journalpost") val path: String,
                   @DefaultValue("isAlive") pingPath: String,
                   @DefaultValue("true") enabled: Boolean): AbstractRestConfig(baseUri,pingPath, JOARK,enabled)