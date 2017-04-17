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
            .build();;

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

        packetDistributer.onReceive(packet1, null);
        assertTrue(receivedPackets.size() == 1);

        packetDistributer.setDefaultHandler(new PacketHandler()
        {
            @Override
            public void handlePacket(final Packet p, final Client c) throws IOException
            {
                // Ignore
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