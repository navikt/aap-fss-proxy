package no.nav.aap.proxy.arena

import javax.xml.namespace.QName
import org.apache.cxf.Bus
import org.apache.cxf.binding.soap.Soap12
import org.apache.cxf.binding.soap.SoapMessage
import org.apache.cxf.endpoint.Client
import org.apache.cxf.ext.logging.LoggingInInterceptor
import org.apache.cxf.ext.logging.LoggingOutInterceptor
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.message.Message.ENDPOINT_ADDRESS
import org.apache.cxf.rt.security.SecurityConstants.CACHE_ISSUED_TOKEN_IN_ENDPOINT
import org.apache.cxf.rt.security.SecurityConstants.PASSWORD
import org.apache.cxf.rt.security.SecurityConstants.USERNAME
import org.apache.cxf.ws.addressing.WSAddressingFeature
import org.apache.cxf.ws.policy.PolicyBuilder
import org.apache.cxf.ws.policy.PolicyEngine
import org.apache.cxf.ws.policy.attachment.reference.RemoteReferenceResolver
import org.apache.neethi.Policy

object TheSTSUtil {
    fun <T> wrapWithSts(port: T, username: String, password: String, stsURI: String): T {
        configureStsForSystemUser(ClientProxy.getClient(port), username, password, stsURI)
        return port
    }
    private fun configureStsForSystemUser(client: Client, location: String, username: String, password: String) =
        with(client) {
            WSAddressingFeature().initialize(this, bus)
            requestContext["security.sts.client"] = createBasicSTSClient(bus, location, username, password)
            requestContext[CACHE_ISSUED_TOKEN_IN_ENDPOINT] = false
            setClientEndpointPolicy(this, RemoteReferenceResolver("", bus.getExtension(PolicyBuilder::class.java)).resolveReference("classpath:stsPolicy.xml"))
        }
    private fun createBasicSTSClient(bus: Bus, location: String, username: String, password: String) =
        TheNAVSTSClient(bus).apply {
            wsdlLocation = "wsdl/ws-trust-1.4-service.wsdl"
            serviceQName = QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/wsdl", "SecurityTokenServiceProvider")
            endpointQName = QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/wsdl", "SecurityTokenServiceSOAP")
            isEnableAppliesTo = false
            isAllowRenewing = false
            client.requestContext[ENDPOINT_ADDRESS] = location
            outInterceptors.add(LoggingOutInterceptor())
            inInterceptors.add(LoggingInInterceptor())
            properties = HashMap<String, Any>().apply {
                this[USERNAME] = username
                this[PASSWORD] = password
            }
        }

    private fun setClientEndpointPolicy(client: Client, policy: Policy) {
        val endpointInfo = client.endpoint.endpointInfo
        val policyEngine = client.bus.getExtension(PolicyEngine::class.java)
        val message = SoapMessage(Soap12.getInstance())
        val endpointPolicy = policyEngine.getClientEndpointPolicy(endpointInfo, null, message)
        policyEngine.setClientEndpointPolicy(endpointInfo, endpointPolicy.updatePolicy(policy, message))
    }
}