package no.nav.aap.api.felles.graphql

import org.springframework.graphql.client.FieldAccessException
import org.springframework.graphql.client.GraphQlClient
import org.springframework.graphql.client.GraphQlTransportException
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.api.felles.error.IrrecoverableGraphQLException.BadGraphQLException
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.api.felles.error.RecoverableGraphQLException
import no.nav.aap.api.felles.error.RecoverableIntegrationException
import no.nav.aap.api.felles.graphql.GraphQLExtensions.oversett
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.springframework.http.HttpStatus

abstract class AbstractGraphQLAdapter(
    client: WebClient, cfg: AbstractRestConfig,
    val handler: GraphQLErrorHandler
) :
    AbstractWebClientAdapter(client, cfg) {

    protected inline fun <reified T : Any> query(
        graphQL: GraphQlClient,
        query: Pair<String, String>,
        vars: Map<String, List<String>>
    ): List<T> =
        runCatching {
            (graphQL
                .documentName(query.first)
                .variables(vars)
                .retrieve(query.second)
                .toEntityList(T::class.java)
                .onErrorMap {
                    when (it) {
                        is FieldAccessException -> it.oversett()
                        is GraphQlTransportException -> BadGraphQLException(
                            BAD_REQUEST,
                            it.message ?: "Transport feil",
                            it
                        )

                        else -> it
                    }
                }
                .retryWhen(retrySpec(log, "/graphql") { it is RecoverableGraphQLException })
                .contextCapture()
                .block() ?: emptyList()).also {
                log.trace(CONFIDENTIAL, "Slo opp liste av {} {}", T::class.java.simpleName, it)
            }
        }.getOrElse {
            handler.handle(it)
        }


    protected inline fun <reified T : Any> query(
        graphQL: GraphQlClient,
        query: Pair<String, String>,
        vars: Map<String, String>
    ): T? =
        runCatching {
            graphQL
                .documentName(query.first)
                .variables(vars)
                .retrieve(query.second)
                .toEntity(T::class.java)
                .onErrorMap {
                    when (it) {
                        is FieldAccessException -> it.oversett()
                        is GraphQlTransportException -> BadGraphQLException(
                            BAD_REQUEST,
                            it.message ?: "Transport feil",
                            it
                        )

                        else -> it
                    }
                }
                .retryWhen(retrySpec(log, "/graphql") { it is RecoverableGraphQLException })
                .contextCapture()
                .block().also {
                    log.trace(CONFIDENTIAL, "Slo opp {} {}", T::class.java.simpleName, it)
                }
        }.getOrElse { t ->
            handler.handle(t)
        }
}

abstract class IrrecoverableGraphQLException(
    status: HttpStatus,
    msg: String,
    cause: Throwable? = null
) : IrrecoverableIntegrationException(
    "$msg (${status.value()})",
    null, cause
) {

    class NotFoundGraphQLException(status: HttpStatus, msg: String) :
        IrrecoverableGraphQLException(status, msg)

    class BadGraphQLException(status: HttpStatus, msg: String, cause: Throwable? = null) :
        IrrecoverableGraphQLException(status, msg, cause)

    class UnauthenticatedGraphQLException(status: HttpStatus, msg: String) :
        IrrecoverableGraphQLException(status, msg)

    class UnauthorizedGraphQLException(status: HttpStatus, msg: String) :
        IrrecoverableGraphQLException(status, msg)
}

abstract class RecoverableGraphQLException(status: HttpStatus, msg: String, cause: Throwable?) :
    RecoverableIntegrationException("${status.value()}-$msg", cause = cause) {

    class UnhandledGraphQLException(status: HttpStatus, msg: String, cause: Throwable? = null) :
        RecoverableGraphQLException(status, msg, cause)
}