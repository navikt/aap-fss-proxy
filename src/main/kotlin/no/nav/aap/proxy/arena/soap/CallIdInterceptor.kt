package no.nav.aap.proxy.arena.soap

import jakarta.xml.bind.JAXBException
import javax.xml.namespace.QName
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.MDCUtil
import org.apache.cxf.binding.soap.SoapMessage
import org.apache.cxf.jaxb.JAXBDataBinding
import org.apache.cxf.phase.AbstractPhaseInterceptor
import org.apache.cxf.phase.Phase
import org.apache.cxf.binding.soap.SoapHeader
import org.apache.cxf.message.Message
import org.apache.cxf.interceptor.Fault

class CallIdInterceptor : AbstractPhaseInterceptor<Message>(Phase.PRE_STREAM) {
    private val log = LoggerUtil.getLogger(javaClass)

    @Throws(Fault::class)
    override fun handleMessage(message: Message) {
        when (message) {
            is SoapMessage ->
                try {
                    val qName = QName("uri:no.nav.applikasjonsrammeverk", "callId")
                    val header = SoapHeader(qName, MDCUtil.callId(), JAXBDataBinding(String::class.java))
                    message.headers.add(header)
                } catch (ex: JAXBException) {
                    log.warn("Error while setting CallId header", ex)
                }
        }
    }
}