package no.nav.aap.proxy

import no.nav.aap.proxy.arena.Fødselsnummer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FødselsnummerTest {
    
    @Test
    fun `valid fødselsnummer is accepted`() {
        val fnr = Fødselsnummer("19819699677")
        assertEquals("19819699677", fnr.fnr)
    }
    
    @Test
    fun `fødselsnummer with wrong length is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            Fødselsnummer("123456789")
        }
    }
    
    @Test
    fun `fødselsnummer with invalid checksum is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            Fødselsnummer("12345678901")
        }
    }
    
    @Test
    fun `toString masks fnr correctly`() {
        val fnr = Fødselsnummer("19819699677")
        assertEquals("Fødselsnummer [fnr=198196*****]", fnr.toString())
    }
}
