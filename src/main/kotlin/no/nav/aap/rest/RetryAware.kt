package no.nav.aap.rest

import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.web.reactive.function.client.WebClientException
import org.springframework.web.reactive.function.client.WebClientResponseException

@Retryable(
    include = [WebClientException::class],
    exclude = [WebClientResponseException.NotFound::class, WebClientResponseException.Forbidden::class, WebClientResponseException.BadRequest::class, WebClientResponseException.Unauthorized::class],
    maxAttemptsExpression = "#{\${rest.retry.attempts:3}}",
    backoff = Backoff(delayExpression = "#{\${rest.retry.delay:1000}}")
)
interface RetryAware