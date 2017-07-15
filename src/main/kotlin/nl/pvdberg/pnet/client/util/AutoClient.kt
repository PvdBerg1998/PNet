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
import nl.pvdberg.pnet.packet.Packet
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AutoClient
/**
 * Adds automatic reconnecting functionality to given Client implementation
 * @param client Client implementation
 * *
 * @param host Host to connect to
 * *
 * @param port Port to connect to
 */
(client: Client,
 /**
  * Returns host
  * @return Host
  */
 val host: String,
 /**
  * Returns port
  * @return Port
  */
 val port: Int) : ClientDecorator(client)
{
    private val logger = LoggerFactory.getLogger(AutoClient::class.java)

    private var onReconnect: Runnable? = null

    /**
     * Sets Runnable event handler which will be called directly after reconnecting
     * @param onReconnect Nullable Runnable
     */
    @Synchronized fun setOnReconnect(onReconnect: Runnable)
    {
        this.onReconnect = onReconnect
    }

    @Synchronized override fun send(packet: Packet): Boolean
    {
        if (!client.isConnected)
        {
            logger.debug("Auto connecting")
            if (!connect(host, port)) return false
        }

        return client.send(packet)
    }

    override fun connect(host: String, port: Int): Boolean
    {
        val connected = client.connect(host, port)
        if (connected && onReconnect != null) onReconnect!!.run()
        return connected
    }
}
