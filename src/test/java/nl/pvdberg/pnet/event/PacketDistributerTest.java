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

package nl.pvdberg.pnet.event;

import nl.pvdberg.pnet.client.Client;
import nl.pvdberg.pnet.packet.Packet;
import nl.pvdberg.pnet.packet.PacketBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PacketDistributerTest
{
    protected static final Packet packet1 = new PacketBuilder(Packet.PacketType.Request)
            .withID((short) 1)
            .build();

    protected static final Packet packet2 = new PacketBuilder(Packet.PacketType.Request)
            .withID((short) 2)
            .build();

    protected PacketDistributer packetDistributer;

    @Before
    public void setUp() throws Exception
    {
        packetDistributer = new PacketDistributer();
    }

    @Test
    public void registeredHandler() throws Exception
    {
        final List<Packet> receivedPackets = new ArrayList<Packet>();

        packetDistributer.addHandler(packet1.getPacketID(), new PacketHandler()
        {
            @Override
            public void handlePacket(final Packet p, final Client c) throws IOException
            {
                receivedPackets.add(p);
            }
        });

        packetDistributer.onReceive(packet1, null);
        assertTrue(receivedPackets.size() == 1);
    }

    @Test
    public void defaultHandler() throws Exception
    {
        final List<Packet> receivedPackets = new ArrayList<Packet>();

        packetDistributer.addHandler(packet1.getPacketID(), new PacketHandler()
        {
            @Override
            public void handlePacket(final Packet p, final Client c) throws IOException
            {
                receivedPackets.add(p);
            }
        });
        packetDistributer.setDefaultHandler(new PacketHandler()
        {
            @Override
            public void handlePacket(final Packet p, final Client c) throws IOException
            {
                receivedPackets.add(p);
            }
        });

        packetDistributer.onReceive(packet2, null);
        assertTrue(receivedPackets.size() == 1);
    }

    @Test
    public void globalHandler() throws Exception
    {
        final List<Packet> receivedPackets = new ArrayList<Packet>();

        final PacketDistributer globalHandler = new PacketDistributer();
        globalHandler.setDefaultHandler(new PacketHandler()
        {
            @Override
            public void handlePacket(final Packet p, final Client c) throws IOException
            {
                receivedPackets.add(p);
            }
        });
        packetDistributer.setGlobalHandler(globalHandler);

        packetDistributer.onReceive(packet1, null);
        assertTrue(receivedPackets.size() == 1);

        packetDistributer.addHandler(packet2.getPacketID(), new PacketHandler()
        {
            @Override
            public void handlePacket(final Packet p, final Client c) throws IOException
            {
                receivedPackets.add(p);
            }
        });

        packetDistributer.onReceive(packet2, null);
        assertTrue(receivedPackets.size() == 3);
    }
}