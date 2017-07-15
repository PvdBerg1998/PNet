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

package nl.pvdberg.pnet.server

import nl.pvdberg.pnet.client.Client
import nl.pvdberg.pnet.factory.ClientFactory
import nl.pvdberg.pnet.event.PNetListener
import nl.pvdberg.pnet.packet.Packet
import nl.pvdberg.pnet.factory.ServerSocketFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.ArrayList

import nl.pvdberg.pnet.threading.ThreadManager.launchThread

class ServerImpl
/**
 * Creates a new Server using given factories
 * @param ssf ServerSocket factory
 * *
 * @param cf Client factory
 */
@Throws(IOException::class)
constructor(private val ssf: ServerSocketFactory, private val cf: ClientFactory) : Server
{
    private val logger = LoggerFactory.getLogger(ServerImpl::class.java)

    private var server: ServerSocket? = null
    private val clients: MutableList<Client>
    private var serverListener: PNetListener? = null

    init
    {

        clients = ArrayList<Client>()
    }

    @Synchronized override fun setListener(serverListener: PNetListener)
    {
        this.serverListener = serverListener
    }

    @Synchronized override fun start(port: Int): Boolean
    {
        logger.debug("Starting server")

        try
        {
            server = ssf.getServerSocket(port)
        }
        catch (e: Exception)
        {
            logger.error("Unable to start server: {} : {}", e.javaClass, e.message)
            return false
        }

        logger.debug("Starting thread")
        launchThread { acceptorThreadImpl() }

        return true
    }

    private fun acceptorThreadImpl()
    {
        while (true)
        {
            // Wait for a connection
            try
            {
                val socket = server!!.accept()
                val client = cf.client

                // Pass events
                client.setClientListener(object : PNetListener
                                         {
                                             override fun onConnect(c: Client)
                                             {
                                                 synchronized(clients) {
                                                     logger.debug("{} connected", c.toString())
                                                     clients.add(c)
                                                 }
                                                 if (serverListener != null) serverListener!!.onConnect(c)
                                             }

                                             override fun onDisconnect(c: Client)
                                             {
                                                 synchronized(clients) {
                                                     logger.debug("{} disconnected", c.toString())
                                                     clients.remove(c)
                                                 }
                                                 if (serverListener != null) serverListener!!.onDisconnect(c)
                                             }

                                             @Throws(IOException::class)
                                             override fun onReceive(p: Packet, c: Client)
                                             {
                                                 if (serverListener != null) serverListener!!.onReceive(p, c)
                                             }
                                         })

                client.socket = socket
            }
            catch (e: SocketException)
            {
                stop()
                break
            }
            catch (e: IOException)
            {
                logger.error("Error in listener thread: {} : {}", e.javaClass, e.message)
                stop()
                break
            }

        }

        logger.debug("Listener thread stopped")
    }

    @Synchronized override fun stop()
    {
        logger.info("Stopping server")

        synchronized(clients) {
            // Close all client threads
            for (client in clients)
            {
                // Prevent ConcurrentModification by removing the event listener
                client.setClientListener(null)
                client.close()
            }
            clients.clear()
        }

        if (server == null) return
        try
        {
            server!!.close()
            logger.debug("ServerSocket closed")
        }
        catch (e: Exception)
        {
            logger.error("Unable to close server: {} : {}", e.javaClass, e.message)
        }

    }
}
