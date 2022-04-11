package no.nav.aap.proxy.inntektskomponent

import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URI

@ConfigurationProperties("inntektskomponent")
@ConstructorBinding
class InntektConfig(baseUri: URI,
                    @DefaultValue("api/v1/hentinntektliste") val path: String,
                    @DefaultValue("api/ping") pingPath: String,
                    @DefaultValue("true") enabled: Boolean): AbstractRestConfig(baseUri,pingPath,enabled)