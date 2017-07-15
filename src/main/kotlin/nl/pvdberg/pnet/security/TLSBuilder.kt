/*
 * MIT License
 *
 * Copyright (c) 2017 Pim van den Berg
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nl.pvdberg.pnet.security

import javax.net.ssl.*
import java.io.IOException
import java.io.InputStream
import java.security.*
import java.security.cert.CertificateException

class TLSBuilder
{
    // Required
    private var host: String? = null
    private var port: Int = 0

    // Optional
    private var customKeyStore = false
    private var keyStoreType: String? = null
    private var keyStoreStream: InputStream? = null
    private var keyStorePassword: CharArray? = null

    private var customTimeout = false
    private var sslTimeout: Int = 0

    /**
     * Sets port
     * @param port Port
     */
    fun withPort(port: Int): TLSBuilder
    {
        this.port = port
        return this
    }

    /**
     * Sets host
     * @param host Host
     */
    fun withHost(host: String): TLSBuilder
    {
        this.host = host
        return this
    }

    /**
     * Sets keystore
     */
    fun withKeyStore(keyStoreType: String, keyStoreStream: InputStream, keyStorePassword: CharArray): TLSBuilder
    {
        customKeyStore = true
        this.keyStoreType = keyStoreType
        this.keyStoreStream = keyStoreStream
        this.keyStorePassword = keyStorePassword

        return this
    }

    /**
     * Sets trust store
     */
    fun withTrustStore(trustStoreType: String, trustStoreStream: InputStream, trustStorePassword: CharArray): TLSBuilder
    {
        return withKeyStore(trustStoreType, trustStoreStream, trustStorePassword)
    }

    /**
     * Sets SSL context timeout
     * @param timeout Timeout in seconds
     */
    fun withTimeout(timeout: Int): TLSBuilder
    {
        customTimeout = true
        sslTimeout = timeout

        return this
    }

    @Throws(KeyStoreException::class, CertificateException::class, NoSuchAlgorithmException::class, IOException::class, UnrecoverableKeyException::class, KeyManagementException::class)
    private fun build(): SSLContext
    {
        val sslContext: SSLContext

        // Init trust store?
        if (customKeyStore)
        {
            val keyStore = KeyStore.getInstance(keyStoreType)
            keyStore.load(keyStoreStream, keyStorePassword)

            // Init key managers
            val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            keyManagerFactory.init(keyStore, keyStorePassword)
            val keyManagers = keyManagerFactory.keyManagers

            // Init trust managers
            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(keyStore)
            val trustManagers = tmf.trustManagers

            // Init SSL context
            sslContext = SSLContext.getInstance(TLS.TLS_CONTEXT_PROTOCOL)
            sslContext.init(keyManagers, trustManagers, SecureRandom())
        }
        else
        {
            sslContext = SSLContext.getDefault()
        }

        // Custom timeout?
        if (customTimeout)
        {
            sslContext.clientSessionContext.sessionTimeout = sslTimeout
            sslContext.serverSessionContext.sessionTimeout = sslTimeout
        }

        return sslContext
    }

    /**
     * Builds a new Socket
     * @return SSLSocket
     */
    @Throws(KeyStoreException::class, CertificateException::class, NoSuchAlgorithmException::class, IOException::class, UnrecoverableKeyException::class, KeyManagementException::class)
    fun buildSocket(): SSLSocket
    {
        if (host == null) throw IllegalStateException("Cannot create socket without host")

        // Get socket
        val s = build().socketFactory.createSocket(host, port) as SSLSocket

        // Set protocols
        s.enabledProtocols = TLS.getUsable(TLS.TLS_PROTOCOLS, s.supportedProtocols)
        s.enabledCipherSuites = TLS.getUsable(TLS.TLS_CIPHER_SUITES, s.supportedCipherSuites)

        return s
    }

    /**
     * Builds a new ServerSocket
     * @return SSLServerSocket
     */
    @Throws(CertificateException::class, UnrecoverableKeyException::class, NoSuchAlgorithmException::class, KeyStoreException::class, KeyManagementException::class, IOException::class)
    fun buildServerSocket(): SSLServerSocket
    {
        // Get socket
        val s = build().serverSocketFactory.createServerSocket(port) as SSLServerSocket

        // Set protocols
        s.enabledProtocols = TLS.getUsable(TLS.TLS_PROTOCOLS, s.supportedProtocols)
        s.enabledCipherSuites = TLS.getUsable(TLS.TLS_CIPHER_SUITES, s.supportedCipherSuites)

        return s
    }
}
