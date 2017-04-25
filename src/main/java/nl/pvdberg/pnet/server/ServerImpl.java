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

package nl.pvdberg.pnet.server;

import nl.pvdberg.pnet.client.Client;
import nl.pvdberg.pnet.factory.ClientFactory;
import nl.pvdberg.pnet.event.PNetListener;
import nl.pvdberg.pnet.packet.Packet;
import nl.pvdberg.pnet.factory.ServerSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import static nl.pvdberg.pnet.threading.ThreadManager.launchThread;

public class ServerImpl implements Server
{
    private final Logger logger;

    private final ServerSocketFactory ssf;
    private final ClientFactory cf;

    private ServerSocket server;
    private final List<Client> clients;
    private PNetListener serverListener;

    /**
     * Creates a new Server using given factories
     * @param ssf ServerSocket factory
     * @param cf Client factory
     */
    public ServerImpl(final ServerSocketFactory ssf, final ClientFactory cf) throws IOException
    {
        this.ssf = ssf;
        this.cf = cf;

        logger = LoggerFactory.getLogger(ServerImpl.class);
        clients = new ArrayList<Client>();
    }

    @Override
    public synchronized void setListener(final PNetListener serverListener)
    {
        this.serverListener = serverListener;
    }

    @Override
    public synchronized boolean start(final int port)
    {
        logger.debug("Starting server");

        try
        {
            server = ssf.getServerSocket(port);
        }
        catch (final Exception e)
        {
            logger.error("Unable to start server: {} : {}", e.getClass(), e.getMessage());
            return false;
        }

        logger.debug("Starting thread");
        launchThread(new Runnable()
        {
            @Override
            public void run()
            {
                acceptorThreadImpl();
            }
        });

        return true;
    }

    private void acceptorThreadImpl()
    {
        while (true)
        {
            // Wait for a connection
            try
            {
                final Socket socket = server.accept();
                final Client client = cf.getClient();

                // Pass events
                client.setClientListener(new PNetListener()
                {
                    @Override
                    public void onConnect(final Client c)
                    {
                        synchronized (clients)
                        {
                            logger.debug("{} connected", c.toString());
                            clients.add(c);
                        }
                        if (serverListener != null) serverListener.onConnect(c);
                    }

                    @Override
                    public void onDisconnect(final Client c)
                    {
                        synchronized (clients)
                        {
                            logger.debug("{} disconnected", c.toString());
                            clients.remove(c);
                        }
                        if (serverListener != null) serverListener.onDisconnect(c);
                    }

                    @Override
                    public void onReceive(final Packet p, final Client c) throws IOException
                    {
                        if (serverListener != null) serverListener.onReceive(p, c);
                    }
                });

                client.setSocket(socket);
            }
            catch (final SocketException e)
            {
                stop();
                break;
            }
            catch (final IOException e)
            {
                logger.error("Error in listener thread: {} : {}", e.getClass(), e.getMessage());
                stop();
                break;
            }
        }

        logger.debug("Listener thread stopped");
    }

    @Override
    public synchronized void stop()
    {
        logger.info("Stopping server");

        synchronized (clients)
        {
            // Close all client threads
            for (final Client client : clients)
            {
                // Prevent ConcurrentModification by removing the event listener
                client.setClientListener(null);
                client.close();
            }
            clients.clear();
        }

        if (server == null) return;
        try
        {
            server.close();
            logger.debug("ServerSocket closed");
        }
        catch (final Exception e)
        {
            logger.error("Unable to close server: {} : {}", e.getClass(), e.getMessage());
        }
    }
}
