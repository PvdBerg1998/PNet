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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TLS
{
    /**
     * Strong TLS protocols
     */
    public static final String[] TLS_PROTOCOLS = { "TLSv1.3", "TLSv1.2" };

    /**
     * Strong cipher suites (best to worst)
     */
    public static final String[] TLC_CIPHER_SUITES =
            {
                    "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305",
                    "TLS_DHE_RSA_WITH_CHACHA20_POLY1305",
                    "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                    "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                    "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
                    "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
                    "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
                    "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
                    "TLS_DHE_RSA_WITH_AES_256_CBC_SHA384",
                    "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
            };


    /**
     * Returns intersection of available and supported
     * @return Intersection of available and supported
     */
    public static String[] getUsable(final String[] available, final String[] supported)
    {
        final List<String> filtered = new ArrayList<String>(available.length);
        final List<String> supportedList = Arrays.asList(supported);

        for (final String s : available)
        {
            if (supportedList.contains(s)) filtered.add(s);
        }

        final String[] filteredArray = new String[filtered.size()];
        filtered.toArray(filteredArray);
        return filteredArray;
    }

    /**
     * Sets the key store properties for TLS
     * @param keyStore Keystore file location
     * @param keyStorePassword Keystore password
     */
    public static void setKeyStore(final String keyStore, final String keyStorePassword)
    {
        System.setProperty("javax.net.ssl.keyStore", keyStore);
        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
    }

    /**
     * Sets the trust store properties for TLS
     * @param trustStore Truststore file location
     * @param trustStorePassword Truststore password
     */
    public static void setTrustStore(final String trustStore, final String trustStorePassword)
    {
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
    }

    /**
     * Enables SSL debug output
     */
    public static void setSSLDebug()
    {
        System.setProperty("javax.net.debug", "SSL");
    }

    /**
     * Creates a new SSL Socket
     * @param host Host to connect to
     * @param port Port to connect to
     * @return SSLSocket
     * @throws IOException when unable to connect
     */
    public static Socket createTLSSocket(final String host, final int port) throws IOException
    {
        final SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        final SSLSocket s = (SSLSocket) sslSocketFactory.createSocket(host, port);

        s.setEnabledProtocols(TLS.getUsable(TLS.TLS_PROTOCOLS, s.getSupportedProtocols()));
        s.setEnabledCipherSuites(TLS.getUsable(TLS.TLC_CIPHER_SUITES, s.getSupportedCipherSuites()));

        return s;
    }

    /**
     * Creates a new SSL ServerSocket
     * @param port Port to listen to
     * @return SSLServerSocket
     * @throws IOException when unable to open server
     */
    public static ServerSocket createTLSServerSocket(final int port) throws IOException
    {
        final SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        final SSLServerSocket s = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);

        s.setEnabledProtocols(TLS.getUsable(TLS.TLS_PROTOCOLS, s.getSupportedProtocols()));
        s.setEnabledCipherSuites(TLS.getUsable(TLS.TLC_CIPHER_SUITES, s.getSupportedCipherSuites()));
        s.setNeedClientAuth(false);

        return s;
    }
}
