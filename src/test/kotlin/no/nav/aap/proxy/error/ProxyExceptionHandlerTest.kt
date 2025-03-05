package no.nav.aap.proxy.error

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.aap.proxy.arena.generated.behandleSakOgAktivitet.BehandleKjoerelisteOgOpprettOppgaveUgyldigInput
import no.nav.aap.proxy.arena.generated.behandleSakOgAktivitet.UgyldigInput
import no.nav.aap.proxy.arena.generated.oppgave.BestillOppgavePersonErInaktiv
import no.nav.aap.proxy.arena.generated.oppgave.BestillOppgavePersonIkkeFunnet
import no.nav.aap.proxy.arena.generated.oppgave.WSPersonErInaktiv
import no.nav.aap.proxy.arena.generated.oppgave.WSPersonIkkeFunnet
import no.nav.aap.util.LoggerUtil
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.springframework.web.context.request.NativeWebRequest

class ProxyExceptionHandlerTest {

    private val loggerMock = mockk<Logger>(relaxed = true)
    private val webRequestMock = mockk<NativeWebRequest>(relaxed = true)

    @BeforeEach
    fun setUp() {
        mockkObject(LoggerUtil)
        every { LoggerUtil.getLogger(any<Class<*>>()) } returns loggerMock
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(LoggerUtil::class)
    }

    @Test
    fun `skal logge feil fra BestillOppgavePersonIkkeFunnet som info`() {
        val personIkkeFunnet = BestillOppgavePersonIkkeFunnet("Person ikke funnet feilmelding", WSPersonIkkeFunnet())
        kastFeil(personIkkeFunnet)

        verifiserInfoLogging()
    }

    @Test
    fun `skal logge feil fra BestillOppgavePersonErInaktiv som info`() {
        val personInaktiv = BestillOppgavePersonErInaktiv("Person inaktiv feilmelding", WSPersonErInaktiv())
        kastFeil(personInaktiv)

        verifiserInfoLogging()
    }

    @Test
    fun `skal logge feil fra BehandleKjoerelisteOgOpprettOppgaveUgyldigInput som info`() {
        val personInaktiv = BehandleKjoerelisteOgOpprettOppgaveUgyldigInput("Mangler kjøreliste", UgyldigInput())
        kastFeil(personInaktiv)

        verifiserInfoLogging()
    }

    @Test
    fun `skal logge generelle feil som error`() {
        val generellFeil = IllegalStateException("En generell feil")
        kastFeil(generellFeil)

        verifiserErrorLogging()
    }

    private fun kastFeil(feil: Exception) {
        ProxyExceptionHandler().catchAll(feil, webRequestMock)
    }

    private fun verifiserInfoLogging() {
        verify { loggerMock.info(any<String>(), any()) }
        verify(exactly = 0) { loggerMock.error(any<String>(), any()) }
    }

    private fun verifiserErrorLogging() {
        verify { loggerMock.error(any<String>(), any()) }
        verify(exactly = 0) { loggerMock.info(any<String>(), any()) }
    }
}