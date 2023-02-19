package no.nav.aap.proxy.arena.soap

import javax.xml.namespace.QName
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.callId
import org.apache.cxf.binding.soap.SoapHeader
import org.apache.cxf.binding.soap.SoapMessage
import org.apache.cxf.jaxb.JAXBDataBinding
import org.apache.cxf.message.Message
import org.apache.cxf.phase.AbstractPhaseInterceptor
import org.apache.cxf.phase.Phase.PRE_STREAM

class ArenaSoapCallIdHeaderInterceptor : AbstractPhaseInterceptor<Message>(PRE_STREAM) {

    override fun handleMessage(msg: Message) =
        runCatching {
            (msg as SoapMessage).headers += SoapHeader(QName(URI, "callId"), callId(), JAXBDataBinding(String::class.java))
        }.getOrElse {
            logger.warn("Error while setting CallId header", it)
        }
    companion object  {
        private val logger = getLogger(ArenaSoapCallIdHeaderInterceptor::class.java)
        private const val URI = "uri:no.nav.applikasjonsrammeverk"
    }
}