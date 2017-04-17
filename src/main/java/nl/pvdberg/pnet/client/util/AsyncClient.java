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
import nl.pvdberg.pnet.event.AsyncListener;
import nl.pvdberg.pnet.event.PNetListener;
import nl.pvdberg.pnet.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

import static nl.pvdberg.pnet.threading.ThreadManager.launchThread;
import static nl.pvdberg.pnet.threading.ThreadManager.waitForCompletion;

public class AsyncClient implements Client
{
    private final Logger logger = LoggerFactory.getLogger(AsyncClient.class);

    private final Client client;
    private PNetListener clientListener;

    private final LinkedBlockingDeque<AsyncPacket> asyncSenderQueue;
    private Future asyncSenderFuture;

    /**
     * Adds asynchronous functionality to given Client implementation
     * @param client Client implementation
     */
    public AsyncClient(final Client client)
    {
        this.client = client;
        asyncSenderQueue = new LinkedBlockingDeque<AsyncPacket>();

        client.setClientListener(new PNetListener()
        {
            @Override
            public void onConnect(final Client c)
            {
                if (clientListener != null)
                    clientListener.onConnect(AsyncClient.this);
            }

            @Override
            public void onDisconnect(final Client c)
            {
                if (clientListener != null)
                    clientListener.onDisconnect(AsyncClient.this);
            }

            @Override
            public void onReceive(final Packet p, final Client c) throws IOException
            {
                if (clientListener != null)
                    clientListener.onReceive(p, AsyncClient.this);
            }
        });
    }

    /**
     * @see Client#connect(String, int)
     * @param asyncListener Nullable completion listener. Contains boolean : true if successfully connected
     */
    public synchronized void connectAsync(final String host, final int port, final AsyncListener asyncListener)
    {
        if (client.isConnected() && asyncListener != null)
        {
            asyncListener.onCompletion(false);
            return;
        }

        logger.debug("Starting connector thread");
        launchThread(new Runnable()
        {
            @Override
            public void run()
            {
                final boolean result = client.connect(host, port);
                if (asyncListener != null) asyncListener.onCompletion(result);
            }
        });
    }

    /**
     * Blocks until all packets are sent asynchronously
     * @see Future#get()
     */
    public synchronized void waitForAsyncCompletion() throws InterruptedException, ExecutionException
    {
        if (asyncSenderFuture != null) waitForCompletion(asyncSenderFuture);
    }

    @Override
    public synchronized boolean send(final Packet packet)
    {
        sendAsync(packet, null);
        return true;
    }

    /**
     * @see Client#send(Packet)
     * @param asyncListener Nullable completion listener. Contains boolean : true if successfully sent
     */
    public synchronized void sendAsync(final Packet packet, final AsyncListener asyncListener)
    {
        logger.debug("Scheduling async Packet");
        asyncSenderQueue.push(new AsyncPacket(packet, asyncListener));

        // Start thread if needed
        if (asyncSenderFuture == null || asyncSenderFuture.isDone())
        {
            asyncSenderFuture = launchThread(new Runnable()
            {
                @Override
                public void run()
                {
                    asyncSenderThreadImpl();
                }
            });
        }
    }

    private void asyncSenderThreadImpl()
    {
        logger.debug("Async sender thread started");
        while (!asyncSenderQueue.isEmpty())
        {
            try
            {
                final AsyncPacket asyncPacket = asyncSenderQueue.takeLast();
                asyncPacket.onComplete(client.send(asyncPacket.getPacket()));
            }
            catch (final InterruptedException e)
            {
                asyncSenderQueue.clear();
                break;
            }
        }
        logger.debug("Async sender thread stopped");
    }

    @Override
    public void setClientListener(final PNetListener clientListener)
    {
        this.clientListener = clientListener;
    }

    @Override
    public synchronized void close()
    {
        client.close();
        if (asyncSenderFuture != null) asyncSenderFuture.cancel(true);
    }

    private static class AsyncPacket
    {
        private final Packet packet;
        private final AsyncListener asyncListener;

        public AsyncPacket(final Packet packet, final AsyncListener asyncListener)
        {
            this.packet = packet;
            this.asyncListener = asyncListener;
        }

        public void onComplete(final boolean result)
        {
            if (asyncListener != null)
                asyncListener.onCompletion(result);
        }

        public Packet getPacket()
        {
            return packet;
        }
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
