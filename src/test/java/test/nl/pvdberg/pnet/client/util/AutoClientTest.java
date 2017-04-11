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

package test.nl.pvdberg.pnet.client.util;

import main.nl.pvdberg.pnet.client.Client;
import main.nl.pvdberg.pnet.client.util.AutoClient;
import main.nl.pvdberg.pnet.client.util.PlainClient;
import main.nl.pvdberg.pnet.event.ReceiveListener;
import main.nl.pvdberg.pnet.packet.Packet;
import main.nl.pvdberg.pnet.packet.PacketBuilder;
import main.nl.pvdberg.pnet.server.Server;
import main.nl.pvdberg.pnet.server.util.PlainServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

public class AutoClientTest
{
    private static final int port = 42365;

    private Server server;
    private Client client;

    @Before
    public void setUp() throws Exception
    {
        server = new PlainServer();
        server.start(port);

        client = new AutoClient(new PlainClient(), "localhost", port);
    }

    @After
    public void tearDown() throws Exception
    {
        server.stop();
    }

    @Test(timeout=1000)
    public void send() throws Exception
    {
        final CountDownLatch latch = new CountDownLatch(1);

        assertFalse(client.isConnected());

        server.setListener(new ReceiveListener()
        {
            @Override
            public void onReceive(final Packet p, final Client c) throws IOException
            {
                latch.countDown();
            }
        });

        final Packet packet = new PacketBuilder(Packet.PacketType.Request).build();
        assertTrue(client.send(packet));

        latch.await();
    }
}