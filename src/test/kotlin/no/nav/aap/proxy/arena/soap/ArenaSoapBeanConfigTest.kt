package no.nav.aap.proxy.arena.soap

import org.apache.cxf.interceptor.Interceptor
import org.apache.cxf.interceptor.InterceptorProvider
import org.apache.cxf.message.Message
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.mock.env.MockEnvironment

class ArenaSoapBeanConfigTest {

    @Test
    fun `soap logging only activates in dev or local`() {
        val provider = TestInterceptorProvider()

        provider.addLoggingInterceptorsIfEnabled(MockEnvironment().apply {
            setActiveProfiles("dev-fss")
        })
        assertThat(provider.inInterceptors()).isNotEmpty
        assertThat(provider.outInterceptors()).isNotEmpty
        assertThat(provider.inFaultInterceptors()).isNotEmpty
        assertThat(provider.outFaultInterceptors()).isNotEmpty

        val prodProvider = TestInterceptorProvider()
        prodProvider.addLoggingInterceptorsIfEnabled(MockEnvironment().apply {
            setActiveProfiles("prod-fss")
        })
        assertThat(prodProvider.inInterceptors()).isEmpty()
        assertThat(prodProvider.outInterceptors()).isEmpty()
        assertThat(prodProvider.inFaultInterceptors()).isEmpty()
        assertThat(prodProvider.outFaultInterceptors()).isEmpty()
    }

    private class TestInterceptorProvider : InterceptorProvider {
        private val _inInterceptors = mutableListOf<Interceptor<out Message>>()
        private val _outInterceptors = mutableListOf<Interceptor<out Message>>()
        private val _inFaultInterceptors = mutableListOf<Interceptor<out Message>>()
        private val _outFaultInterceptors = mutableListOf<Interceptor<out Message>>()

        override fun getInInterceptors() = _inInterceptors
        override fun getOutInterceptors() = _outInterceptors
        override fun getInFaultInterceptors() = _inFaultInterceptors
        override fun getOutFaultInterceptors() = _outFaultInterceptors

        fun inInterceptors() = _inInterceptors
        fun outInterceptors() = _outInterceptors
        fun inFaultInterceptors() = _inFaultInterceptors
        fun outFaultInterceptors() = _outFaultInterceptors
    }
}
