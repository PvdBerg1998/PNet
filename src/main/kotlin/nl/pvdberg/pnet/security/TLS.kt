package nl.pvdberg.pnet.security

import java.util.ArrayList
import java.util.Arrays

object TLS
{
    var TLS_CONTEXT_PROTOCOL = "TLSv1.2"

    /**
     * Strong TLS protocols
     */
    var TLS_PROTOCOLS: MutableList<String> = ArrayList()

    /**
     * Strong cipher suites (best to worst)
     */
    var TLS_CIPHER_SUITES: MutableList<String> = ArrayList()

    init
    {
        TLS.TLS_PROTOCOLS.add("TLSv1.2")

        TLS.TLS_CIPHER_SUITES.add("TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256")
        TLS.TLS_CIPHER_SUITES.add("TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256")

        TLS.TLS_CIPHER_SUITES.add("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384")
        TLS.TLS_CIPHER_SUITES.add("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384")

        TLS.TLS_CIPHER_SUITES.add("TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256")
        TLS.TLS_CIPHER_SUITES.add("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256")

        TLS.TLS_CIPHER_SUITES.add("TLS_DHE_RSA_WITH_AES_256_GCM_SHA384")
        TLS.TLS_CIPHER_SUITES.add("TLS_DHE_RSA_WITH_AES_128_GCM_SHA256")
    }

    /**
     * Returns intersection of available and supported
     * @return Intersection of available and supported
     */
    internal fun getUsable(available: List<String>, supported: Array<String>): Array<String>
    {
        val filtered = ArrayList<String>(available.size)
        val supportedList = Arrays.asList(*supported)

        for (s in available)
        {
            if (supportedList.contains(s)) filtered.add(s)
        }

        val filteredArray = arrayOfNulls<String>(filtered.size)
        filtered.toTypedArray()
        return filteredArray
    }

    /**
     * Enables SSL debug output
     */
    fun setSSLDebug()
    {
        System.setProperty("javax.net.debug", "SSL")
    }
}
