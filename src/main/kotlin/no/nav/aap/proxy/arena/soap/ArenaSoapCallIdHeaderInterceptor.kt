package no.nav.aap.proxy.arena.soap

import jakarta.xml.bind.JAXBException
import javax.xml.namespace.QName
import no.nav.aap.util.MDCUtil
import org.apache.cxf.binding.soap.SoapHeader
import org.apache.cxf.binding.soap.SoapMessage
import org.apache.cxf.jaxb.JAXBDataBinding
import org.apache.cxf.message.Message
import org.apache.cxf.phase.AbstractPhaseInterceptor
import org.apache.cxf.phase.Phase
import org.slf4j.LoggerFactory

class ArenaSoapCallIdHeaderInterceptor : AbstractPhaseInterceptor<Message>(Phase.PRE_STREAM) {
     val logger = LoggerFactory.getLogger(ArenaSoapCallIdHeaderInterceptor::class.java)

    override fun handleMessage(message: Message) {
        try {
            val qName = QName("uri:no.nav.applikasjonsrammeverk", "callId")
            val header = SoapHeader(qName, MDCUtil.callId(), JAXBDataBinding(String::class.java))
            (message as SoapMessage).headers.add(header)
        }
        catch (ex: JAXBException) {
            logger.warn("Error while setting CallId header", ex)
        }
    }
}