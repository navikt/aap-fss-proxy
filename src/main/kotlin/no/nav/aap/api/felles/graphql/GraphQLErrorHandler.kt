package no.nav.aap.api.felles.graphql

import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException

/* Denne kalles når retry har gitt opp */
interface GraphQLErrorHandler {
    fun handle(e : Throwable) : Nothing = when (e) {
        is IntegrationException -> throw e
        else -> throw IrrecoverableIntegrationException(e.message, cause = e)
    }

}