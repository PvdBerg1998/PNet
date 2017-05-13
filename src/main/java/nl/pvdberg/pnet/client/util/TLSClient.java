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

package nl.pvdberg.pnet.client.util;

import nl.pvdberg.pnet.client.ClientImpl;
import nl.pvdberg.pnet.factory.SocketFactory;
import nl.pvdberg.pnet.security.TLSBuilder;

import javax.net.ssl.SSLSocket;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;

public class TLSClient extends ClientDecorator
{
    private SSLSocket sslSocket;

    /**
     * Creates a new Client using TLS with default trust store
     */
    public TLSClient()
    {
        super(new ClientImpl(
                new SocketFactory()
                {
                    @Override
                    public Socket getSocket(final String host, final int port) throws Exception
                    {
                        return new TLSBuilder()
                                .withHost(host)
                                .withPort(port)
                                .buildSocket();
                    }
                })
        );
    }

    /**
     * Creates a new Client using TLS with given trust store
     */
    public TLSClient(final byte[] trustStore, final char[] trustStorePassword, final String trustStoreType)
    {
        super(new ClientImpl(
                new SocketFactory()
                {
                    @Override
                    public Socket getSocket(final String host, final int port) throws Exception
                    {
                        return new TLSBuilder()
                                .withHost(host)
                                .withPort(port)
                                .withTrustStore(trustStoreType, new ByteArrayInputStream(trustStore), trustStorePassword)
                                .buildSocket();
                    }
                })
        );
    }

    @Override
    public synchronized void setSocket(final Socket socket) throws IOException
    {
        client.setSocket(socket);
        sslSocket = (SSLSocket) socket;
    }

    /**
     * Returns internal SSLSocket
     * @return SSLSocket
     */
    public synchronized SSLSocket getSSLSocket()
    {
        return sslSocket;
    }
}
