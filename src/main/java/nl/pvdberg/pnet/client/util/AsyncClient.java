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
import nl.pvdberg.pnet.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

import static nl.pvdberg.pnet.threading.ThreadManager.launchThread;
import static nl.pvdberg.pnet.threading.ThreadManager.waitForCompletion;

public class AsyncClient extends ClientExtension
{
    private final Logger logger = LoggerFactory.getLogger(AsyncClient.class);

    private final LinkedBlockingDeque<AsyncPacket> asyncSenderQueue;
    private Future asyncSenderFuture;

    /**
     * Adds asynchronous functionality to given Client implementation
     * @param client Client implementation
     */
    public AsyncClient(final Client client)
    {
        super(client);

        asyncSenderQueue = new LinkedBlockingDeque<AsyncPacket>();
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

    /**
     * Calls {@link AsyncClient#sendAsync(Packet, AsyncListener, boolean) sendAsync(Packet, AsyncListener, false)}
     */
    public synchronized void sendAsync(final Packet packet, final AsyncListener asyncListener)
    {
        sendAsync(packet, asyncListener, false);
    }

    /**
     * @see Client#send(Packet)
     * @param asyncListener Nullable completion listener. Contains boolean : true if successfully sent
     * @param topPriority Whether to add this Packet at the head of the queue
     */
    public synchronized void sendAsync(final Packet packet, final AsyncListener asyncListener, final boolean topPriority)
    {
        logger.debug("Scheduling async Packet, top priority: {}", topPriority);

        if (topPriority)
        {
            asyncSenderQueue.addFirst(new AsyncPacket(packet, asyncListener));
        }
        else
        {
            asyncSenderQueue.addLast(new AsyncPacket(packet, asyncListener));
        }

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
                final AsyncPacket asyncPacket = asyncSenderQueue.takeFirst();
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
}
