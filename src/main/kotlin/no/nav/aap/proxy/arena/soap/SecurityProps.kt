package no.nav.operations.soap

import javax.security.auth.callback.Callback
import javax.security.auth.callback.CallbackHandler
import org.apache.wss4j.common.ext.WSPasswordCallback
import org.apache.wss4j.dom.WSConstants
import org.apache.wss4j.dom.handler.WSHandlerConstants
import org.apache.wss4j.dom.handler.WSHandlerConstants.*

class SecurityProps(user: String, password: String) : HashMap<String, Any>() {
    init {
        this[ACTION] = USERNAME_TOKEN
        this[USER] = user
        this[PASSWORD_TYPE] = WSConstants.PW_TEXT
        this[PW_CALLBACK_REF] = CallbackHandler { callbacks: Array<Callback> ->
            val passwordCallback = callbacks[0] as WSPasswordCallback
            passwordCallback.password = password
        }
    }
}