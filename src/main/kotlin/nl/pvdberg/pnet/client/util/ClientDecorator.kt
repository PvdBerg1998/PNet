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
import nl.pvdberg.pnet.event.PNetListener
import nl.pvdberg.pnet.packet.Packet

import java.io.IOException
import java.net.InetAddress
import java.net.Socket

open class ClientDecorator(protected val client: Client) : Client
{
    protected var clientListener: PNetListener? = null

    init
    {

        client.setClientListener(object : PNetListener
                                 {
                                     override fun onConnect(c: Client)
                                     {
                                         if (clientListener != null)
                                             clientListener!!.onConnect(this@ClientDecorator)
                                     }

                                     override fun onDisconnect(c: Client)
                                     {
                                         if (clientListener != null)
                                             clientListener!!.onDisconnect(this@ClientDecorator)
                                     }

                                     @Throws(IOException::class)
                                     override fun onReceive(p: Packet, c: Client)
                                     {
                                         if (clientListener != null)
                                             clientListener!!.onReceive(p, this@ClientDecorator)
                                     }
                                 })
    }

    override fun setClientListener(clientListener: PNetListener)
    {
        this.clientListener = clientListener
    }

    override fun connect(host: String, port: Int): Boolean
    {
        return client.connect(host, port)
    }

    override fun send(packet: Packet): Boolean
    {
        return client.send(packet)
    }

    override fun close()
    {
        client.close()
    }

    override val isConnected: Boolean
        get() = client.isConnected

    override val inetAddress: InetAddress
        get() = client.inetAddress

    override var socket: Socket
        get() = client.socket
        @Throws(IOException::class)
        set(socket)
        {
            client.socket = socket
        }

    override fun toString(): String
    {
        return client.toString()
    }
}
