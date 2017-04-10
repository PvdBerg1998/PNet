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

package main.nl.pvdberg.pnet.server.util;

import main.nl.pvdberg.pnet.client.Client;
import main.nl.pvdberg.pnet.factory.ClientFactory;
import main.nl.pvdberg.pnet.client.util.PlainClient;
import main.nl.pvdberg.pnet.event.PNetListener;
import main.nl.pvdberg.pnet.server.Server;
import main.nl.pvdberg.pnet.factory.ServerSocketFactory;
import main.nl.pvdberg.pnet.server.ServerImpl;

import java.io.IOException;
import java.net.ServerSocket;

public class PlainServer implements Server
{
    private final Server server;

    /**
     * Creates a new normal Server
     */
    public PlainServer() throws IOException
    {
        super();

        server = new ServerImpl(
                new ServerSocketFactory()
                {
                    @Override
                    public ServerSocket getServerSocket(final int port) throws IOException
                    {
                        return new ServerSocket(port);
                    }
                },
                new ClientFactory()
                {
                    @Override
                    public Client getClient()
                    {
                        return new PlainClient();
                    }
                }
        );
    }

    @Override
    public void setListener(final PNetListener serverListener)
    {
        server.setListener(serverListener);
    }

    @Override
    public void start(final int port) throws IOException
    {
        server.start(port);
    }

    @Override
    public void stop()
    {
        server.stop();
    }
}
