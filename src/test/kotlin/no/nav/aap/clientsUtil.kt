package no.nav.aap

import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

fun createShortCircuitWebClient(jsonResponse: String): WebClient {
    val clientResponse: ClientResponse = ClientResponse
        .create(HttpStatus.OK)
        .header("Content-Type","application/json")
        .body(jsonResponse).build()

    val shortCircuitingExchangeFunction = ExchangeFunction {
        Mono.just(clientResponse)
    }

    return WebClient.builder().exchangeFunction(shortCircuitingExchangeFunction).build()
}

fun createShortCircuitWebClientQueued(vararg jsonResponses: String): WebClient {
    val responseList = ArrayDeque(jsonResponses.map {
        ClientResponse
            .create(HttpStatus.OK)
            .header("Content-Type","application/json")
            .body(it).build()
    })

    val shortCircuitingExchangeFunction = ExchangeFunction {
        Mono.just(responseList.removeFirst())
    }

    return WebClient.builder().exchangeFunction(shortCircuitingExchangeFunction).build()
}