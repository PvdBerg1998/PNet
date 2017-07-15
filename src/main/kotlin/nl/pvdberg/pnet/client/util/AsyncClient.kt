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

package nl.pvdberg.pnet.client.util

import nl.pvdberg.pnet.client.Client
import nl.pvdberg.pnet.event.AsyncListener
import nl.pvdberg.pnet.packet.Packet
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingDeque

import nl.pvdberg.pnet.threading.ThreadManager.launchThread
import nl.pvdberg.pnet.threading.ThreadManager.waitForCompletion

class AsyncClient
/**
 * Adds asynchronous functionality to given Client implementation
 * @param client Client implementation
 */
(client: Client) : ClientDecorator(client)
{
    private val logger = LoggerFactory.getLogger(AsyncClient::class.java)

    private val asyncSenderQueue: LinkedBlockingDeque<AsyncPacket>
    private var asyncSenderFuture: Future<*>? = null

    init
    {

        asyncSenderQueue = LinkedBlockingDeque<AsyncPacket>()
    }

    /**
     * @see Client.connect
     * @param asyncListener Nullable completion listener. Contains boolean : true if successfully connected
     */
    @Synchronized fun connectAsync(host: String, port: Int, asyncListener: AsyncListener?)
    {
        if (client.isConnected && asyncListener != null)
        {
            asyncListener.onCompletion(false)
            return
        }

        logger.debug("Starting connector thread")
        launchThread {
            val result = client.connect(host, port)
            asyncListener?.onCompletion(result)
        }
    }

    /**
     * Blocks until all packets are sent asynchronously
     * @see Future.get
     */
    @Synchronized @Throws(InterruptedException::class, ExecutionException::class)
    fun waitForAsyncCompletion()
    {
        if (asyncSenderFuture != null) waitForCompletion(asyncSenderFuture)
    }

    /**
     * Calls [sendAsync(Packet, AsyncListener, false)][AsyncClient.sendAsync]
     */
    @Synchronized fun sendAsync(packet: Packet, asyncListener: AsyncListener)
    {
        sendAsync(packet, asyncListener, false)
    }

    /**
     * @see Client.send
     * @param asyncListener Nullable completion listener. Contains boolean : true if successfully sent
     * *
     * @param topPriority Whether to add this Packet at the head of the queue
     */
    @Synchronized fun sendAsync(packet: Packet, asyncListener: AsyncListener, topPriority: Boolean)
    {
        logger.debug("Scheduling async Packet, top priority: {}", topPriority)

        if (topPriority)
        {
            asyncSenderQueue.addFirst(AsyncPacket(packet, asyncListener))
        }
        else
        {
            asyncSenderQueue.addLast(AsyncPacket(packet, asyncListener))
        }

        // Start thread if needed
        if (asyncSenderFuture == null || asyncSenderFuture!!.isDone)
        {
            asyncSenderFuture = launchThread { asyncSenderThreadImpl() }
        }
    }

    private fun asyncSenderThreadImpl()
    {
        logger.debug("Async sender thread started")
        while (!asyncSenderQueue.isEmpty())
        {
            try
            {
                val asyncPacket = asyncSenderQueue.takeFirst()
                asyncPacket.onComplete(client.send(asyncPacket.packet))
            }
            catch (e: InterruptedException)
            {
                asyncSenderQueue.clear()
                break
            }

        }
        logger.debug("Async sender thread stopped")
    }

    @Synchronized override fun close()
    {
        client.close()
        if (asyncSenderFuture != null) asyncSenderFuture!!.cancel(true)
    }

    private class AsyncPacket(val packet: Packet, private val asyncListener: AsyncListener?)
    {

        fun onComplete(result: Boolean)
        {
            asyncListener?.onCompletion(result)
        }
    }
}
