package no.nav.aap.api.felles.error

import java.net.URI

abstract class IntegrationException(msg: String?, uri: URI? = null, cause: Throwable? = null) :
    RuntimeException(msg, cause)

open class RecoverableIntegrationException(
    msg: String?,
    uri: URI? = null,
    cause: Throwable? = null
) : IntegrationException(msg, uri, cause)

open class IrrecoverableIntegrationException(
    msg: String?,
    uri: URI? = null,
    cause: Throwable? = null
) : IntegrationException(msg, uri, cause)

