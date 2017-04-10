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

package main.nl.pvdberg.pnet.client;

import main.nl.pvdberg.pnet.event.PNetListener;
import main.nl.pvdberg.pnet.packet.Packet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public interface Client
{
    /**
     * Sets the event listener
     * @param clientListener Nullable event listener
     */
    void setClientListener(final PNetListener clientListener);

    /**
     * Connects to given host:port
     * @throws IllegalStateException when Client is not closed
     * @return Successful
     */
    boolean connect(final String host, final int port);

    /**
     * Directly sets socket in Client
     * @param socket Socket to be used
     * @throws IOException when unable to use given Socket
     * @throws IllegalStateException when Client is not closed
     */
    void setSocket(final Socket socket) throws IOException;

    /**
     * Sends given Packet
     * @param packet Packet to send
     * @return Successful
     */
    boolean send(final Packet packet);

    /**
     * Closes listener thread and socket of this Client
     */
    void close();

    /**
     * Returns whether the Client has an active connection
     * @return Connected
     */
    boolean isConnected();

    /**
     * Returns InetAddress of this Client
     * @return InetAddress of this Client
     */
    InetAddress getInetAddress();

    /**
     * Returns current Socket
     * @return Socket
     */
    Socket getSocket();
}
