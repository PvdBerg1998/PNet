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

import java.io.*;

public class Packet
{
    private final PacketType packetType;
    private final short packetID;
    private final int dataLength;
    private final byte[] data;

    public enum PacketType
    {
        Request,
        Reply;

        public static final PacketType[] fastValues = values();
    }

    /**
     * Creates a new immutable Packet
     * @param packetType Packet Type
     * @param packetID Packet ID
     * @param data Packet Data
     */
    public Packet(final PacketType packetType, final short packetID, final byte[] data)
    {
        this.packetType = packetType;
        this.packetID = packetID;
        dataLength = data.length;
        this.data = data;
    }

    /**
     * Returns Packet Type
     * @return Packet Type
     */
    public PacketType getPacketType()
    {
        return packetType;
    }

    /**
     * Returns whether Packet is of type Request
     * @return PacketType is Request
     */
    public boolean isRequest()
    {
        return packetType == PacketType.Request;
    }

    /**
     * Returns whether Packet is of type Reply
     * @return PacketType is Reply
     */
    public boolean isReply()
    {
        return packetType == PacketType.Reply;
    }

    /**
     * Returns Packet ID
     * @return Packet ID
     */
    public short getPacketID()
    {
        return packetID;
    }

    /**
     * Returns Data length
     * @return Data length
     */
    public int getDataLength()
    {
        return dataLength;
    }

    /**
     * Returns Packet data
     * @return Data
     */
    public byte[] getData()
    {
        return data;
    }

    /**
     * Writes Packet into DataOutputStream
     * @param out DataOutputStream to write into
     * @throws IOException when unable to write to stream
     */
    public void write(final DataOutputStream out) throws IOException
    {
        // Packet Type
        out.writeByte(packetType.ordinal());

        // Packet ID
        out.writeShort(packetID);

        // Data Length
        out.writeInt(dataLength);

        // Data
        out.write(data);
    }

    /**
     * Reads a Packet from raw input data
     * @param in DataInputStream to fromStream from
     * @return Packet created from input
     * @throws IOException when unable to read from stream
     */
    public static Packet fromStream(final DataInputStream in) throws IOException
    {
        // Packet Type
        final Packet.PacketType packetType = Packet.PacketType.fastValues[in.readByte()];

        // Packet ID
        final short packetID = in.readShort();

        // Data Length
        final int dataLength = in.readInt();

        // Data
        final byte[] data = new byte[dataLength];
        in.readFully(data);

        return new Packet(
                packetType,
                packetID,
                data
        );
    }

    @Override
    public String toString()
    {
        return "Type: [" + packetType + "] ID: [" + packetID + "] Data: [" + dataLength + " bytes]";
    }
}
