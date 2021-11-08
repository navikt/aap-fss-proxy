package no.nav.aap.error

class IntegrationException(msg: String? , cause: Throwable? = null) :RuntimeException(msg,cause) {
}