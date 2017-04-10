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

package test.nl.pvdberg.pnet;

import main.nl.pvdberg.pnet.client.Client;
import main.nl.pvdberg.pnet.client.util.PlainClient;
import main.nl.pvdberg.pnet.event.ReceiveListener;
import main.nl.pvdberg.pnet.packet.Packet;
import main.nl.pvdberg.pnet.packet.PacketBuilder;
import main.nl.pvdberg.pnet.packet.PacketReader;
import main.nl.pvdberg.pnet.server.Server;
import main.nl.pvdberg.pnet.server.util.PlainServer;
import main.nl.pvdberg.pnet.threading.ThreadManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BenchmarkTest
{
    private Server server;
    private Client client;
    private long start;
    private long end;

    @Before
    public void setup() throws Exception
    {
        server = new PlainServer();
        server.start(666);

        client = new PlainClient();
        client.connect("localhost", 666);
    }

    @After
    public void teardown() throws Exception
    {
        server.stop();
        client.close();
    }

    @Test
    public void testEmptyPacketsPerSecond() throws Exception
    {
        final int amount = 100000;

        final Packet packet = new PacketBuilder(Packet.PacketType.Request)
                .build();

        server.setListener(new ReceiveListener()
        {
            @Override
            public void onReceive(final Packet p, final Client c) throws IOException
            {
                assertEquals(Packet.PacketType.Request.ordinal(), p.getPacketType().ordinal());
            }
        });

        start = System.currentTimeMillis();
        for (int i = 0; i < amount; i++)
        {
            assertTrue(client.send(packet));
        }
        end = System.currentTimeMillis();

        System.out.println(amount / ((end - start) / 1000f) + " packets per second");
    }

    @Test
    public void testMBPerSecond() throws Exception
    {
        final int amount = 100000;

        final byte[] randomData = new byte[48000];
        new Random().nextBytes(randomData);

        final Packet packet = new PacketBuilder(Packet.PacketType.Request)
                .withBytes(randomData)
                .build();

        server.setListener(new ReceiveListener()
        {
            @Override
            public void onReceive(final Packet p, final Client c) throws IOException
            {
                assertEquals(randomData.length, p.getData().length);
            }
        });

        start = System.currentTimeMillis();
        for (int i = 0; i < amount; i++)
        {
            assertTrue(client.send(packet));
        }
        end = System.currentTimeMillis();

        System.out.println((randomData.length / 1000000f * amount) / ((end - start) / 1000f) + " MB per second");
    }
}
