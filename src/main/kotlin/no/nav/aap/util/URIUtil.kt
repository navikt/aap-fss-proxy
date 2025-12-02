package no.nav.aap.util

import java.net.URI
import org.springframework.http.HttpHeaders
import org.springframework.web.util.UriComponentsBuilder

object URIUtil {

    fun uri(base : String, path : String) = uri(URI.create(base), path)
    fun uri(base : URI, path : String, queryParams : HttpHeaders? = null) = builder(base, path, queryParams).build().toUri()
    private fun builder(base : URI, path : String, queryParams : HttpHeaders?) = 
        UriComponentsBuilder.fromUri(base).pathSegment(path).apply {
            queryParams?.forEach { key, values -> queryParam(key, *values.toTypedArray()) }
        }
    fun uri(base : URI, queryParams : HttpHeaders) = uri(base, "/", queryParams)
}