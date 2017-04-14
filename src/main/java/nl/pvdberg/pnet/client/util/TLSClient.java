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

import nl.pvdberg.pnet.client.Client;
import nl.pvdberg.pnet.client.ClientImpl;
import nl.pvdberg.pnet.event.PNetListener;
import nl.pvdberg.pnet.factory.SocketFactory;
import nl.pvdberg.pnet.packet.Packet;
import nl.pvdberg.pnet.security.TLS;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;

public class TLSClient implements Client
{
    private final Client client;

    private SSLSocket sslSocket;

    /**
     * Creates a new Client using TLS with default trust store
     */
    public TLSClient()
    {
        client = new ClientImpl(
                new SocketFactory()
                {
                    @Override
                    public Socket getSocket(final String host, final int port) throws Exception
                    {
                        return TLS.createTLSSocket(host, port);
                    }
                }
        );
    }

    /**
     * Creates a new Client using TLS with given trust store
     * @see nl.pvdberg.pnet.security.TLS#createTLSSocket(String, int, InputStream, char[], String)
     */
    public TLSClient(final InputStream trustStoreStream, final char[] trustStorePassword, final String trustStoreType)
    {
        client = new ClientImpl(
                new SocketFactory()
                {
                    @Override
                    public Socket getSocket(final String host, final int port) throws Exception
                    {
                        return TLS.createTLSSocket(host, port, trustStoreStream, trustStorePassword, trustStoreType);
                    }
                }
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

    @Override
    public void setClientListener(final PNetListener clientListener)
    {
        client.setClientListener(clientListener);
    }

    @Override
    public boolean connect(final String host, final int port)
    {
        return client.connect(host, port);
    }

    @Override
    public boolean send(final Packet packet)
    {
        return client.send(packet);
    }

    @Override
    public void close()
    {
        client.close();
    }

    @Override
    public boolean isConnected()
    {
        return client.isConnected();
    }

    @Override
    public InetAddress getInetAddress()
    {
        return client.getInetAddress();
    }

    @Override
    public Socket getSocket()
    {
        return client.getSocket();
    }
}
