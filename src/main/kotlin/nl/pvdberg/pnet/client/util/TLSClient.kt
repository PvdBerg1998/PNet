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

import nl.pvdberg.pnet.client.ClientImpl
import nl.pvdberg.pnet.factory.SocketFactory
import nl.pvdberg.pnet.security.TLSBuilder

import javax.net.ssl.SSLSocket
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.Socket

class TLSClient : ClientDecorator
{
    /**
     * Returns internal SSLSocket
     * @return SSLSocket
     */
    @get:Synchronized var sslSocket: SSLSocket? = null
        private set

    /**
     * Creates a new Client using TLS with default trust store
     */
    constructor() : super(ClientImpl(
            SocketFactory { host, port ->
                TLSBuilder()
                        .withHost(host)
                        .withPort(port)
                        .buildSocket()
            })
    )
    {
    }

    /**
     * Creates a new Client using TLS with given trust store
     */
    constructor(trustStore: ByteArray, trustStorePassword: CharArray, trustStoreType: String) : super(ClientImpl(
            SocketFactory { host, port ->
                TLSBuilder()
                        .withHost(host)
                        .withPort(port)
                        .withTrustStore(trustStoreType, ByteArrayInputStream(trustStore), trustStorePassword)
                        .buildSocket()
            })
    )
    {
    }

    override var socket: Socket
        get
        @Synchronized @Throws(IOException::class)
        set(socket)
        {
            client.socket = socket
            sslSocket = socket as SSLSocket
        }
}
