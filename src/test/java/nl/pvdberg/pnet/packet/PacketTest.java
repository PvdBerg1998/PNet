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

package nl.pvdberg.pnet.packet;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Random;

import static org.junit.Assert.*;

public class PacketTest
{
    @Test
    public void write() throws Exception
    {
        final byte[] data = new byte[128];
        new Random().nextBytes(data);

        final Packet packet = new Packet(
                Packet.PacketType.Request,
                (short) 0,
                data
        );

        // Create expected protocol output
        final ByteArrayOutputStream bout1 = new ByteArrayOutputStream();
        final DataOutputStream dout1 = new DataOutputStream(bout1);
        dout1.writeByte(Packet.PacketType.Request.ordinal());
        dout1.writeShort(0);
        dout1.writeInt(data.length);
        dout1.write(data);

        final ByteArrayOutputStream bout2 = new ByteArrayOutputStream();
        final DataOutputStream dout2 = new DataOutputStream(bout2);
        packet.write(dout2);

        assertArrayEquals(bout1.toByteArray(), bout2.toByteArray());
    }

    @Test
    public void fromStream() throws Exception
    {
        final byte[] data = new byte[128];
        new Random().nextBytes(data);

        // Create protocol input
        final ByteArrayOutputStream bout1 = new ByteArrayOutputStream();
        final DataOutputStream dout1 = new DataOutputStream(bout1);
        dout1.writeByte(Packet.PacketType.Request.ordinal());
        dout1.writeShort(0);
        dout1.writeInt(data.length);
        dout1.write(data);

        final DataInputStream din1 = new DataInputStream(new ByteArrayInputStream(bout1.toByteArray()));

        final Packet packet = Packet.fromStream(din1);

        assertEquals(Packet.PacketType.Request, packet.getPacketType());
        assertEquals(0, packet.getPacketID());
        assertEquals(data.length, packet.getDataLength());
        assertArrayEquals(data, packet.getData());
    }

}