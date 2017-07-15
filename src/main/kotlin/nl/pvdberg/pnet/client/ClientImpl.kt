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

package nl.pvdberg.pnet.client

import nl.pvdberg.pnet.event.PNetListener
import nl.pvdberg.pnet.factory.SocketFactory
import nl.pvdberg.pnet.packet.Packet
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.io.*
import java.net.InetAddress
import java.net.Socket
import java.net.SocketException

import nl.pvdberg.pnet.threading.ThreadManager.launchThread

class ClientImpl
/**
 * Creates a new Client
 */
(private val sf: SocketFactory) : Client
{
    private val logger = LoggerFactory.getLogger(ClientImpl::class.java)

    @get:Synchronized override var socket: Socket? = null
        @Synchronized @Throws(IOException::class)
        set(socket)
        {
            if (this.socket != null && !this.socket!!.isClosed) throw IllegalStateException("Client not closed")

            field = socket
            socket.setKeepAlive(false)
            dataInputStream = DataInputStream(BufferedInputStream(socket.getInputStream()))
            dataOutputStream = DataOutputStream(BufferedOutputStream(socket.getOutputStream()))

            logger.debug("Starting thread")
            launchThread { listenerThreadImpl() }

            if (clientListener != null) clientListener!!.onConnect(this)
        }
    private var dataInputStream: DataInputStream? = null
    private var dataOutputStream: DataOutputStream? = null

    private var clientListener: PNetListener? = null

    @Synchronized override fun setClientListener(clientListener: PNetListener)
    {
        this.clientListener = clientListener
    }

    @Synchronized override fun connect(host: String, port: Int): Boolean
    {
        if (this.socket != null && !this.socket!!.isClosed) throw IllegalStateException("Client not closed")
        if (host.isEmpty() || port == -1) throw IllegalStateException("Host and port are not set")

        logger.info("Connecting to {}:{}", host, port)

        try
        {
            socket = sf.getSocket(host, port)
            logger.debug("Connected")
            return true
        }
        catch (e: Exception)
        {
            logger.error("Unable to connect: {} : {}", e.javaClass, e.message)
            return false
        }

    }

    private fun listenerThreadImpl()
    {
        while (true)
        {
            val packet: Packet

            try
            {
                // Block while waiting for a Packet
                packet = Packet.fromStream(dataInputStream)
            }
            catch (e: SocketException)
            {
                // Ignore : socket is closed
                close()
                break
            }
            catch (e: EOFException)
            {
                // Ignore : socket is closed
                close()
                break
            }
            catch (e: IOException)
            {
                logger.error("Error in listener thread: {} : {}", e.javaClass, e.message)
                close()
                break
            }

            logger.debug("Received packet: {{}}", packet)

            // Fire event
            if (clientListener != null)
            {
                try
                {
                    clientListener!!.onReceive(packet, this)
                }
                catch (e: IOException)
                {
                    logger.warn("Unable to handle Packet: {} : {}", e.javaClass, e.message)
                }
                catch (e: Exception)
                {
                    logger.error("Exception while handling onReceive: {} : {}", e.javaClass, e.message)
                }

            }
        }

        logger.debug("Listener thread stopped")
    }

    @Synchronized override fun send(packet: Packet): Boolean
    {
        if (!isConnected) return false

        try
        {
            logger.debug("Sending packet: {{}}", packet)
            packet.write(dataOutputStream)
            dataOutputStream!!.flush()
            return true
        }
        catch (e: IOException)
        {
            logger.error("Error while sending packet {{}} : {} : {}", e.javaClass, e.message)
            return false
        }

    }

    @Synchronized override fun close()
    {
        if (this.socket == null) return
        if (this.socket!!.isClosed) return

        logger.info("Closing client")

        try
        {
            this.socket!!.close()
            logger.debug("Socket closed")
        }
        catch (e: IOException)
        {
            logger.error("Unable to close socket: {} : {}", e.javaClass, e.message)
        }

        if (clientListener != null) clientListener!!.onDisconnect(this)
    }

    override val isConnected: Boolean
        @Synchronized get() = this.socket != null && this.socket!!.isConnected && !this.socket!!.isClosed

    override val inetAddress: InetAddress
        @Synchronized get() = this.socket!!.inetAddress

    @Synchronized override fun toString(): String
    {
        return this.socket!!.toString()
    }
}
