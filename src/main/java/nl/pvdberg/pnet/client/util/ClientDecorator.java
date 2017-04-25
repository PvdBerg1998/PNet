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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ClientDecorator implements Client
{
    protected final Client client;
    protected PNetListener clientListener;

    public ClientDecorator(final Client client)
    {
        this.client = client;

        client.setClientListener(new PNetListener()
        {
            @Override
            public void onConnect(final Client c)
            {
                if (clientListener != null)
                    clientListener.onConnect(ClientDecorator.this);
            }

            @Override
            public void onDisconnect(final Client c)
            {
                if (clientListener != null)
                    clientListener.onDisconnect(ClientDecorator.this);
            }

            @Override
            public void onReceive(final Packet p, final Client c) throws IOException
            {
                if (clientListener != null)
                    clientListener.onReceive(p, ClientDecorator.this);
            }
        });
    }

    @Override
    public void setClientListener(final PNetListener clientListener)
    {
        this.clientListener = clientListener;
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

    @Override
    public String toString()
    {
        return client.toString();
    }
}
