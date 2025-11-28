package no.nav.aap.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object LoggerUtil {

    fun getLogger(forClass : Class<*>) : Logger = LoggerFactory.getLogger(forClass)

}