package no.nav.aap.proxy.arena

import java.security.Principal
import no.nav.aap.util.LoggerUtil.getLogger
import org.apache.cxf.Bus
import org.apache.cxf.ws.security.SecurityConstants
import org.apache.cxf.ws.security.tokenstore.SecurityToken
import org.apache.cxf.ws.security.tokenstore.TokenStore
import org.apache.cxf.ws.security.tokenstore.TokenStoreFactory
import org.apache.cxf.ws.security.trust.STSClient

class TheNAVSTSClient(b: Bus) : STSClient(b) {

    private val log = getLogger(javaClass)

    lateinit var tokenStore: TokenStore
    override fun useSecondaryParameters() = false

    override fun requestSecurityToken(appliesTo: String, action: String, requestType: String, binaryExchange: String): SecurityToken {
        val key = "systemSAML"
        val principal = SluttBruker("systemuser")
         ensureTokenStoreExists()
        var token = tokenStore.getToken(key)
        val keyUtenSignatur = stripJwtSignatur(key)
        if (token == null) {
            log.debug("Missing token cache key {}, fetching it from STS", keyUtenSignatur)
            token = super.requestSecurityToken(appliesTo, action, requestType, binaryExchange)
            token.principal = principal
            tokenStore.add(key, token)
        }
        else if (token.isExpired) {
            log.debug("Token cache key {} is expired ({}) fetching a new one from STS",
                    keyUtenSignatur,
                    token.expires)
            tokenStore.remove(key)
            token = super.requestSecurityToken(appliesTo, action, requestType, binaryExchange)
            token.principal = principal
            tokenStore.add(key, token)
        }
        else {
            log.debug("Retrived token, cache key {} from tokenStore", keyUtenSignatur)
        }
            log.trace("Retrived token: {}", tokenToString(token))
        return token
    }

    private fun ensureTokenStoreExists() {
        if (tokenStore == null) {
            try {
                createTokenStore()
            }
            catch (e: Exception) {
                // for kompat cxf 3.4
                throw IllegalStateException("Kan ikke opprette TokenStore", e)
            }
        }
    }

    private fun createTokenStore() {
        if (tokenStore == null) {
            log.info("Creating tokenStore")
            tokenStore =
                TokenStoreFactory.newInstance().newTokenStore(SecurityConstants.TOKEN_STORE_CACHE_INSTANCE, message)
        }
    }

    private data class SluttBruker  ( val username: String) : Principal {
        override fun getName() = username
    }


        /**
         * A JWT consists of &lt;base64 encoded header&gt;.&lt;base64 encoded
         * body&gt;.&lt;base64 encoded signature&gt;
         *
         * @return if key is JWT - &lt;base64 encoded header&gt;.&lt;base64 encoded
         * body&gt; <br></br>
         * else - `key`
         */
        private fun stripJwtSignatur(key: String): String {
            val lastDot = key.lastIndexOf('.')
            val end = if (lastDot == -1) key.length else lastDot
            return key.substring(0, end)
        }

        private fun tokenToString(token: SecurityToken): String {
            return (token.javaClass.simpleName + "<" +
                    "id=" + token.id + ", "
                    + "wsuId=" + token.wsuId + ", "
                    + "principal=" + token.principal + ", "
                    + "created=" + token.created + ", "
                    + "expires=" + token.expires + ", "
                    + "isExpired=" + token.isExpired + ", "
                    + ">")
        }
}