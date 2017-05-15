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
    protected static final Packet.PacketType TYPE = Packet.PacketType.Reply;
    protected static final short ID = 123;
    protected static final boolean BOOLEAN = true;
    protected static final byte BYTE = 1;
    protected static final byte[] BYTES = new byte[] {1, 2};
    protected static final double DOUBLE = 0.123d;
    protected static final float FLOAT = 0.321f;
    protected static final int INT = Integer.MAX_VALUE;
    protected static final long LONG = Long.MIN_VALUE;
    protected static final short SHORT = Short.MIN_VALUE;
    protected static final String STRING = "Hello!";

    @Test
    public void buildAndRead() throws Exception
    {
        final Packet packet = new PacketBuilder(TYPE)
                .withID(ID)
                .withBoolean(BOOLEAN)
                .withByte(BYTE)
                .withBytes(BYTES)
                .withDouble(DOUBLE)
                .withFloat(FLOAT)
                .withInt(INT)
                .withLong(LONG)
                .withShort(SHORT)
                .withString(STRING)
                .build();

        final PacketReader packetReader = new PacketReader(packet);

        assertEquals(ID, packetReader.getPacket().getPacketID());
        assertEquals(BOOLEAN, packetReader.readBoolean());
        assertEquals(BYTE, packetReader.readByte());
        assertArrayEquals(BYTES, packetReader.readBytes());
        assertEquals(DOUBLE, packetReader.readDouble(), 0.0001d);
        assertEquals(FLOAT, packetReader.readFloat(), 0.0001f);
        assertEquals(INT, packetReader.readInt());
        assertEquals(LONG, packetReader.readLong());
        assertEquals(SHORT, packetReader.readShort());
        assertEquals(STRING, packetReader.readString());
    }
}