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

package nl.pvdberg.pnet.client;

import nl.pvdberg.pnet.event.PNetListener;
import nl.pvdberg.pnet.factory.SocketFactory;
import nl.pvdberg.pnet.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import static nl.pvdberg.pnet.threading.ThreadManager.launchThread;

public class ClientImpl implements Client
{
    private final Logger logger;

    private final SocketFactory sf;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private PNetListener clientListener;

    /**
     * Creates a new Client
     */
    public ClientImpl(final SocketFactory sf)
    {
        this.sf = sf;

        logger = LoggerFactory.getLogger(ClientImpl.class);
    }

    @Override
    public synchronized void setClientListener(final PNetListener clientListener)
    {
        this.clientListener = clientListener;
    }

    @Override
    public synchronized boolean connect(final String host, final int port)
    {
        if (socket != null && !socket.isClosed()) throw new IllegalStateException("Client not closed");
        if (host.isEmpty() || port == -1) throw new IllegalStateException("Host and port are not set");

        logger.info("Connecting to {}:{}", host, port);

        try
        {
            setSocket(sf.getSocket(host, port));
            logger.debug("Connected");
            return true;
        }
        catch (final Exception e)
        {
            logger.error("Unable to connect: {} : {}", e.getClass(), e.getMessage());
            return false;
        }
    }

    @Override
    public synchronized void setSocket(final Socket socket) throws IOException
    {
        if (this.socket != null && !this.socket.isClosed()) throw new IllegalStateException("Client not closed");

        this.socket = socket;
        socket.setKeepAlive(false);
        dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        logger.debug("Starting thread");
        launchThread(new Runnable()
        {
            @Override
            public void run()
            {
                listenerThreadImpl();
            }
        });

        if (clientListener != null) clientListener.onConnect(this);
    }

    private void listenerThreadImpl()
    {
        while (true)
        {
            final Packet packet;

            try
            {
                // Block while waiting for a Packet
                packet = Packet.fromStream(dataInputStream);
            }
            catch (final SocketException e)
            {
                // Ignore : socket is closed
                close();
                break;
            }
            catch (final EOFException e)
            {
                // Ignore : socket is closed
                close();
                break;
            }
            catch (final IOException e)
            {
                logger.error("Error in listener thread: {} : {}", e.getClass(), e.getMessage());
                close();
                break;
            }

            logger.debug("Received packet: {{}}", packet);

            // Fire event
            if (clientListener != null)
            {
                try
                {
                    clientListener.onReceive(packet, this);
                }
                catch (final IOException e)
                {
                    logger.warn("Unable to handle Packet: {} : {}", e.getClass(), e.getMessage());
                }
                catch (final Exception e)
                {
                    logger.error("Exception while handling onReceive: {} : {}", e.getClass(), e.getMessage());
                }
            }
        }

        logger.debug("Listener thread stopped");
    }

    @Override
    public synchronized boolean send(final Packet packet)
    {
        if (!isConnected()) return false;

        try
        {
            logger.debug("Sending packet: {{}}", packet);
            packet.write(dataOutputStream);
            dataOutputStream.flush();
            return true;
        }
        catch (final IOException e)
        {
            logger.error("Error while sending packet {{}} : {} : {}", e.getClass(), e.getMessage());
            return false;
        }
    }

    @Override
    public synchronized void close()
    {
        if (socket == null) return;
        if (socket.isClosed()) return;

        logger.info("Closing client");

        try
        {
            socket.close();
            logger.debug("Socket closed");
        }
        catch (final IOException e)
        {
            logger.error("Unable to close socket: {} : {}", e.getClass(), e.getMessage());
        }

        if (clientListener != null) clientListener.onDisconnect(this);
    }

    @Override
    public synchronized boolean isConnected()
    {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    @Override
    public synchronized InetAddress getInetAddress()
    {
        return socket.getInetAddress();
    }

    @Override
    public synchronized Socket getSocket()
    {
        return socket;
    }

    @Override
    public synchronized String toString()
    {
        return socket.toString();
    }
}
