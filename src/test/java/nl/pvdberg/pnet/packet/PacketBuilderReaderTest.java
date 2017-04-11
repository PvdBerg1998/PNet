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

import static org.junit.Assert.*;

public class PacketBuilderReaderTest
{
    @Test
    public void buildAndRead() throws Exception
    {
        final Packet packet = new PacketBuilder(Packet.PacketType.Reply)
                .withBoolean(true)
                .withByte((byte) 1)
                .withBytes(new byte[] {1, 2})
                .withDouble(0.123d)
                .withFloat(0.321f)
                .withID((short) 123)
                .withInt(Integer.MAX_VALUE)
                .withLong(Long.MIN_VALUE)
                .withShort(Short.MIN_VALUE)
                .withString("hello!")
                .build();

        final PacketReader packetReader = new PacketReader(packet);
        assertEquals(true, packetReader.readBoolean());
        assertEquals(1, packetReader.readByte());

        final byte[] bytes = new byte[2];
        packetReader.readBytes(bytes, 2);
        assertArrayEquals(new byte[] {1, 2}, bytes);

        assertEquals(0.123d, packetReader.readDouble(), 0.0001d);
        assertEquals(0.321f, packetReader.readFloat(), 0.0001f);
        assertEquals(123, packetReader.getPacket().getPacketID());
        assertEquals(Integer.MAX_VALUE, packetReader.readInt());
        assertEquals(Long.MIN_VALUE, packetReader.readLong());
        assertEquals(Short.MIN_VALUE, packetReader.readShort());
        assertEquals("hello!", packetReader.readString());
    }
}