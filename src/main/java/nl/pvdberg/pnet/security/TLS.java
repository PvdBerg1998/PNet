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
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TLS
{
    private static final String TLS_CONTEXT_PROTOCOL = "TLSv1.2";

    /**
     * Strong TLS protocols
     */
    private static final String[] TLS_PROTOCOLS = { "TLSv1.2", "TLSv1.1" };

    /**
     * Strong cipher suites (best to worst)
     */
    private static final String[] TLC_CIPHER_SUITES =
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
    private static String[] getUsable(final String[] available, final String[] supported)
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
     * Enables SSL debug output
     */
    public static void setSSLDebug()
    {
        System.setProperty("javax.net.debug", "SSL");
    }

    /**
     * Creates a new SSL Socket using given trust store.
     * @param host Host to connect to
     * @param port Port to connect to
     * @param trustStoreStream Trust store inputstream
     * @param trustStorePassword Trust store password
     * @param trustStoreType Trust store type (eg. JKS)
     * @return SSLSocket
     * @throws IOException when unable to connect
     * @throws KeyStoreException when unable to load trust store
     * @throws CertificateException when unable to load trust store
     * @throws NoSuchAlgorithmException when unable to load trust store
     * @throws UnrecoverableKeyException when unable to load trust store
     * @throws KeyManagementException when unable to load trust store
     */
    public static Socket createTLSSocket(final String host, final int port, final InputStream trustStoreStream, final char[] trustStorePassword, final String trustStoreType) throws
            KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException
    {
        final SSLSocket s;

        // Init trust store
        final KeyStore trustStore = KeyStore.getInstance(trustStoreType);
        trustStore.load(trustStoreStream, trustStorePassword);

        // Init key managers
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(trustStore, trustStorePassword);
        final KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

        // Init trust managers
        final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        final TrustManager[] trustManagers = tmf.getTrustManagers();

        // Init SSL context
        final SSLContext sslContext = SSLContext.getInstance(TLS_CONTEXT_PROTOCOL);
        sslContext.init(keyManagers, trustManagers, new SecureRandom());

        // Get socket
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        s = (SSLSocket) sslSocketFactory.createSocket(host, port);

        s.setEnabledProtocols(TLS.getUsable(TLS.TLS_PROTOCOLS, s.getSupportedProtocols()));
        s.setEnabledCipherSuites(TLS.getUsable(TLS.TLC_CIPHER_SUITES, s.getSupportedCipherSuites()));

        return s;
    }

    /**
     * Creates a new SSL Socket using default trust store.
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
     * @param keyStoreStream Key store inputstream
     * @param keyStorePassword Key store password
     * @param keyStoreType Key store type (eg. JKS)
     * @return SSLServerSocket
     * @throws IOException when unable to open server
     * @throws KeyStoreException when unable to load key store
     * @throws CertificateException when unable to load key store
     * @throws NoSuchAlgorithmException when unable to load key store
     * @throws UnrecoverableKeyException when unable to load key store
     * @throws KeyManagementException when unable to load key store
     */
    public static ServerSocket createTLSServerSocket(final int port, final InputStream keyStoreStream, final char[] keyStorePassword, final String keyStoreType) throws
            KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException
    {
        // Init key store
        final KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(keyStoreStream, keyStorePassword);

        // Init key managers
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyStorePassword);
        final KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

        // Init SSL context
        final SSLContext sslContext = SSLContext.getInstance(TLS_CONTEXT_PROTOCOL);
        sslContext.init(keyManagers, null, new SecureRandom());

        // Get socket
        final SSLServerSocketFactory socketFactory = sslContext.getServerSocketFactory();
        final SSLServerSocket s = (SSLServerSocket) socketFactory.createServerSocket(port);

        s.setEnabledProtocols(TLS.getUsable(TLS.TLS_PROTOCOLS, s.getSupportedProtocols()));
        s.setEnabledCipherSuites(TLS.getUsable(TLS.TLC_CIPHER_SUITES, s.getSupportedCipherSuites()));
        s.setNeedClientAuth(false);

        return s;
    }
}
