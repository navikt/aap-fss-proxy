package no.nav.aap.proxy.norg

import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.util.Constants
import no.nav.aap.util.Constants.NORG
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URI

@ConstructorBinding
@ConfigurationProperties(NORG)
class NorgConfig(
        baseUri: URI,
        @DefaultValue("/api/v1/arbeidsfordeling/enheter/bestmatch") val path: String,
        @DefaultValue("internal/isAlive") pingPath: String,
        @DefaultValue("true") enabled: Boolean) : AbstractRestConfig(baseUri, pingPath, enabled)