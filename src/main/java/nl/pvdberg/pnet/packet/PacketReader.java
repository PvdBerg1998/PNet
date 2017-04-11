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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PacketReader
{
    private final Packet packet;
    private final ByteArrayInputStream byteArrayInputStream;
    private final DataInputStream dataInputStream;

    /**
     * Provides an easy way to read data from a Packet
     * Note: data has to be read in the same order as it was written!
     */
    public PacketReader(final Packet packet)
    {
        this.packet = packet;
        byteArrayInputStream = new ByteArrayInputStream(packet.getData());
        dataInputStream = new DataInputStream(byteArrayInputStream);
    }

    /**
     * Reads a byte
     * @return Byte
     * @throws IOException when unable to read
     */
    public synchronized byte readByte() throws IOException
    {
        return dataInputStream.readByte();
    }

    /**
     * Reads byte array into output
     * @param out Byte array output
     * @param dataLength Amount of bytes
     * @throws IOException when not enough data is available
     */
    public synchronized void readBytes(final byte[] out, final int dataLength) throws IOException
    {
        final int dataRead = dataInputStream.read(out, 0, dataLength);
        if (dataRead != dataLength) throw new IOException("Not enough data available");
    }

    /**
     * Reads an integer
     * @return Integer
     * @throws IOException when unable to read
     */
    public synchronized int readInt() throws IOException
    {
        return dataInputStream.readInt();
    }

    /**
     * Reads a String
     * @return UTF-8 String
     * @throws IOException when unable to read
     */
    public synchronized String readString() throws IOException
    {
        return dataInputStream.readUTF();
    }

    /**
     * Reads a boolean
     * @return Boolean
     * @throws IOException when unable to read
     */
    public synchronized boolean readBoolean() throws IOException
    {
        return dataInputStream.readBoolean();
    }

    /**
     * Reads a float
     * @return Float
     * @throws IOException when unable to read
     */
    public synchronized float readFloat() throws IOException
    {
        return dataInputStream.readFloat();
    }

    /**
     * Reads a double
     * @return Double
     * @throws IOException when unable to read
     */
    public synchronized double readDouble() throws IOException
    {
        return dataInputStream.readDouble();
    }

    /**
     * Reads a long
     * @return Long
     * @throws IOException when unable to read
     */
    public synchronized long readLong() throws IOException
    {
        return dataInputStream.readLong();
    }

    /**
     * Reads a short
     * @return Short
     * @throws IOException when unable to read
     */
    public synchronized short readShort() throws IOException
    {
        return dataInputStream.readShort();
    }

    /**
     * Returns internal Packet
     * @return Packet
     */
    public Packet getPacket()
    {
        return packet;
    }
}
