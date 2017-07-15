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

package nl.pvdberg.pnet.packet

import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.DataOutputStream
import java.nio.charset.Charset

/**
 * Provides an easy way to build a Packet.
 * Data has to be written in the same order as it will be read
 * @param packetType Type of packet to create
 * @param charset Charset to use for strings
 */
class PacketBuilder(val packetType: Packet.PacketType, val charset: Charset) : Closeable
{
    private val byteArrayOutputStream = ByteArrayOutputStream()
    private val dataOutputStream = DataOutputStream(byteArrayOutputStream)
    var packetID: Short = 0

    /**
     * Current data as a byte array
     * @return Byte array
     */
    val bytes: ByteArray get() = byteArrayOutputStream.toByteArray()

    /**
     * Current data in a Packet
     * @return Packet
     */
    val packet: Packet get()
    {
        dataOutputStream.flush()
        return Packet(
                packetType,
                packetID,
                byteArrayOutputStream.toByteArray()
        )
    }

    operator fun plus(b: Byte)
    {
        dataOutputStream.writeByte(b.toInt())
    }

    operator fun plus(b: ByteArray)
    {
        dataOutputStream.writeInt(b.size)
        dataOutputStream.write(b)
    }

    operator fun plus(s: String)
    {
        this + s.toByteArray(charset)
    }

    operator fun plus(i: Int)
    {
        dataOutputStream.writeInt(i)
    }

    operator fun plus(b: Boolean)
    {
        dataOutputStream.writeBoolean(b)
    }

    operator fun plus(f: Float)
    {
        dataOutputStream.writeFloat(f)
    }

    operator fun plus(d: Double)
    {
        dataOutputStream.writeDouble(d)
    }

    operator fun plus(l: Long)
    {
        dataOutputStream.writeLong(l)
    }

    operator fun plus(s: Short)
    {
        dataOutputStream.writeShort(s.toInt())
    }

    override fun close()
    {
        dataOutputStream.close()
    }
}
