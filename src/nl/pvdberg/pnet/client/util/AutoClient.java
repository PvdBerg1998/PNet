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
import nl.pvdberg.pnet.event.PNetListener;
import nl.pvdberg.pnet.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class AutoClient implements Client
{
    private final Logger logger = LoggerFactory.getLogger(AutoClient.class);

    private final Client client;
    private final String host;
    private final int port;

    private Runnable onReconnect;

    /**
     * Adds automatic reconnecting functionality to given Client implementation
     * @param client Client implementation
     * @param host Host to connect to
     * @param port Port to connect to
     */
    public AutoClient(final Client client, final String host, final int port)
    {
        this.client = client;
        this.host = host;
        this.port = port;
    }

    /**
     * Sets Runnable event handler which will be called directly after reconnecting
     * @param onReconnect Nullable Runnable
     */
    public synchronized void setOnReconnect(final Runnable onReconnect)
    {
        this.onReconnect = onReconnect;
    }

    @Override
    public synchronized boolean send(final Packet packet)
    {
        if (!client.isConnected())
        {
            logger.debug("Auto connecting");
            if (!client.connect(host, port)) return false;
            if (onReconnect != null) onReconnect.run();
        }

        return client.send(packet);
    }

    /**
     * Returns host
     * @return Host
     */
    public String getHost()
    {
        return host;
    }

    /**
     * Returns port
     * @return Port
     */
    public int getPort()
    {
        return port;
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
    public void setSocket(final Socket socket) throws IOException
    {
        client.setSocket(socket);
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
