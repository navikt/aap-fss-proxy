import jakarta.xml.ws.BindingProvider
import jakarta.xml.ws.Holder
import jakarta.xml.ws.handler.Handler
import java.util.*
import javax.naming.ServiceUnavailableException
import javax.xml.datatype.XMLGregorianCalendar
import javax.xml.namespace.QName
import kotlin.collections.HashMap
import no.nav.operations.soap.SecurityProps
import org.apache.cxf.configuration.jsse.TLSClientParameters
import org.apache.cxf.endpoint.Client
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.interceptor.Interceptor
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.message.Message
import org.apache.cxf.transport.http.HTTPConduit
import org.apache.cxf.ws.addressing.WSAddressingFeature
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor
import org.jalla.Bruker
import org.jalla.FaultFeilIInputMsg
import org.jalla.FaultGeneriskMsg
import org.jalla.SakVedtakPortType
import org.jalla.SaksInfoListe
import org.slf4j.LoggerFactory

class SoapArenaAdapter(url: String, username: String, password: String) {
    private lateinit var arenaSakVedtakService: SakVedtakPortType

    init {
        try {
            arenaSakVedtakService = createArenaSakVedtakServiceClient(url, SecurityProps(username, password)).build()
            log.info("arenaSakVedtakService initialisert")
        }
        catch (e: Exception) {
            log.error("Feil under initialisering av arenaSakVedtakService {}, {}", e.message, e.cause)
        }
    }

    @Throws(ServiceUnavailableException::class)
    fun fetchSaker(brukerId: String?, brukertype: String?, saksId: Int?, fomDato: XMLGregorianCalendar?,
                   tomDato: XMLGregorianCalendar?, tema: String?, lukket: Boolean): SaksInfoListe {
        val bruker  = Holder(Bruker().apply {
            setBrukerId(brukerId)
            brukertypeKode = brukertype
        })
        val saker  = Holder(SaksInfoListe())
        try {
            arenaSakVedtakService.hentSaksInfoListeV2(bruker, saksId, fomDato, tomDato, tema, lukket, saker)
        }
        catch (e: FaultFeilIInputMsg) {
            log.error("Feil input til hentSaksInfov2. FaultInfo: {}", e.faultInfo, e)
            throw ServiceUnavailableException(e.message)
        }
        catch (e: FaultGeneriskMsg) {
            log.error("Feil ved hentSaksInfov2. FaultInfo: {}", e.faultInfo, e)
            throw ServiceUnavailableException(e.message)
        }
        catch (e: Exception) {
            log.error("Ukjent feil under kall på hentSaksInfov2: {}, {}", e.message, e.cause, e)
            throw ServiceUnavailableException(e.message)
        }
        return saker.value
    }

    private fun createArenaSakVedtakServiceClient(url: String, securityProps: SecurityProps) =
         CXFClient(SakVedtakPortType::class.java)
            .address(url)
            .withOutInterceptor(WSS4JOutInterceptor(securityProps))
            .timeout(TIMEOUT, TIMEOUT)

    companion object {
        private val log = LoggerFactory.getLogger(SoapArenaAdapter::class.java)
        private const val TIMEOUT = 12000
    }
}

class CXFClient<T> {
    private val factoryBean = JaxWsProxyFactoryBean()
    private val serviceClass: Class<T>
    private val handlerChain = mutableListOf<Handler<*>>()
    private var connectionTimeout = 10000
    private var receiveTimeout = 10000
    private var allowChunking = false

    constructor(serviceClass: Class<T>) {
        Objects.requireNonNull(serviceClass)
       // factoryBean.features.add(LoggingFeature())
        factoryBean.features.add(WSAddressingFeature())
        factoryBean.properties = HashMap()
        this.serviceClass = serviceClass
    }

    fun address(url: String?): CXFClient<T> {
        factoryBean.address = url
        return this
    }

    fun wsdl(url: String?): CXFClient<T> {
        factoryBean.wsdlURL = url
        return this
    }

    fun withProperty(key: String?, value: Any?): CXFClient<T> {
        factoryBean.properties[key] = value
        return this
    }

    fun timeout(connectionTimeout: Int, receiveTimeout: Int): CXFClient<T> {
        this.connectionTimeout = connectionTimeout
        this.receiveTimeout = receiveTimeout
        return this
    }

    fun enableMtom(): CXFClient<T> {
        factoryBean.properties["mtom-enabled"] = true
        return this
    }

    fun withHandler(handler: Handler<*>, vararg moreHandlers: Handler<*>): CXFClient<T> {
        handlerChain.add(handler)
        handlerChain.addAll(moreHandlers)
        return this
    }

    fun allowChunking(allowChunking: Boolean): CXFClient<T> {
        this.allowChunking = allowChunking
        return this
    }

    fun serviceName(serviceName: QName): CXFClient<T> {
        factoryBean.serviceName = serviceName
        return this
    }

    fun endpointName(endpointName: QName?): CXFClient<T> {
        factoryBean.endpointName = endpointName
        return this
    }

    fun withOutInterceptor(interceptor: Interceptor<out Message>,
                           vararg moreInterceptors: Interceptor<out Message>): CXFClient<T> {
        val outInterceptors: MutableList<Interceptor<out Message>> = factoryBean.outInterceptors
        outInterceptors.add(interceptor)
        outInterceptors.addAll(moreInterceptors)
        return this
    }

    fun configureClient(address: String, wsdl: String, service: QName, endpoint: QName): CXFClient<T> {
        address(address)
        wsdl(wsdl)
        serviceName(service)
        endpointName(endpoint)
        return this
    }

    fun build(): T {
      //  factoryBean.features.add(TimeoutFeature(receiveTimeout, connectionTimeout))
       // factoryBean.features.add(ChunkingFeature(allowChunking))
        val portType = factoryBean.create(serviceClass)
        val client = ClientProxy.getClient(portType)
        disableCNCheckIfConfigured(client)
        (portType as BindingProvider).binding.handlerChain = handlerChain
        return portType
    }

    companion object {
        private fun disableCNCheckIfConfigured(client: Client) {
            val httpConduit = client.getConduit() as HTTPConduit
            val tlsClientParameters =
                if (httpConduit.tlsClientParameters != null) httpConduit.tlsClientParameters else TLSClientParameters()
            tlsClientParameters.isDisableCNCheck = true
            httpConduit.tlsClientParameters = tlsClientParameters
        }
    }
}