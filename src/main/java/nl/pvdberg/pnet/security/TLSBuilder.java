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

package nl.pvdberg.pnet.security;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

public class TLSBuilder
{
    // Required
    private String host;
    private int port;

    // Optional
    private boolean customKeyStore = false;
    private String keyStoreType;
    private InputStream keyStoreStream;
    private char[] keyStorePassword;

    private boolean customTimeout = false;
    private int sslTimeout;

    /**
     * Sets port
     * @param port Port
     */
    public TLSBuilder withPort(final int port)
    {
        this.port = port;
        return this;
    }

    /**
     * Sets host
     * @param host Host
     */
    public TLSBuilder withHost(final String host)
    {
        this.host = host;
        return this;
    }

    /**
     * Sets keystore
     */
    public TLSBuilder withKeyStore(final String keyStoreType, final InputStream keyStoreStream, final char[] keyStorePassword)
    {
        customKeyStore = true;
        this.keyStoreType = keyStoreType;
        this.keyStoreStream = keyStoreStream;
        this.keyStorePassword = keyStorePassword;

        return this;
    }

    /**
     * Sets trust store
     */
    public TLSBuilder withTrustStore(final String trustStoreType, final InputStream trustStoreStream, final char[] trustStorePassword)
    {
        return withKeyStore(trustStoreType, trustStoreStream, trustStorePassword);
    }

    /**
     * Sets SSL context timeout
     * @param timeout Timeout in seconds
     */
    public TLSBuilder withTimeout(final int timeout)
    {
        customTimeout = true;
        sslTimeout = timeout;

        return this;
    }

    private SSLContext build() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException
    {
        final SSLContext sslContext;

        // Init trust store?
        if (customKeyStore)
        {
            final KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(keyStoreStream, keyStorePassword);

            // Init key managers
            final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword);
            final KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

            // Init trust managers
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            final TrustManager[] trustManagers = tmf.getTrustManagers();

            // Init SSL context
            sslContext = SSLContext.getInstance(TLS.TLS_CONTEXT_PROTOCOL);
            sslContext.init(keyManagers, trustManagers, new SecureRandom());
        }
        else
        {
            sslContext = SSLContext.getDefault();
        }

        // Custom timeout?
        if (customTimeout)
        {
            sslContext.getClientSessionContext().setSessionTimeout(sslTimeout);
            sslContext.getServerSessionContext().setSessionTimeout(sslTimeout);
        }

        return sslContext;
    }

    /**
     * Builds a new Socket
     * @return SSLSocket
     */
    public SSLSocket buildSocket() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException
    {
        if (host == null) throw new IllegalStateException("Cannot create socket without host");

        // Get socket
        final SSLSocket s = (SSLSocket) build().getSocketFactory().createSocket(host, port);

        // Set protocols
        s.setEnabledProtocols(TLS.getUsable(TLS.TLS_PROTOCOLS, s.getSupportedProtocols()));
        s.setEnabledCipherSuites(TLS.getUsable(TLS.TLS_CIPHER_SUITES, s.getSupportedCipherSuites()));

        return s;
    }

    /**
     * Builds a new ServerSocket
     * @return SSLServerSocket
     */
    public SSLServerSocket buildServerSocket() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        // Get socket
        final SSLServerSocket s = (SSLServerSocket) build().getServerSocketFactory().createServerSocket(port);

        // Set protocols
        s.setEnabledProtocols(TLS.getUsable(TLS.TLS_PROTOCOLS, s.getSupportedProtocols()));
        s.setEnabledCipherSuites(TLS.getUsable(TLS.TLS_CIPHER_SUITES, s.getSupportedCipherSuites()));

        return s;
    }
}
