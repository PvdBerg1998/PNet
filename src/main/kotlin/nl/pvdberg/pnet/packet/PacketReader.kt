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

import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.nio.charset.Charset

inline fun <T> Packet.read(charset: Charset = Packet.DEFAULT_CHARSET, apply: PacketReader.() -> T) =
        PacketReader(this, charset).apply()

/**
 * Provides an easy way to read a Packet.
 * Data has to be read in the same order as it was written
 * @param packet Packet to read
 * @param charset Charset to use for strings
 */
class PacketReader(val packet: Packet, val charset: Charset = Packet.DEFAULT_CHARSET)
{
    private val dataInputStream: DataInputStream = DataInputStream(ByteArrayInputStream(packet.data))

    val byte get() = dataInputStream.readByte()
    val bytes: ByteArray get()
    {
        val dataLength = dataInputStream.readInt()
        val data = ByteArray(dataLength)

        val dataRead = dataInputStream.read(data, 0, dataLength)
        require(dataRead == dataRead) {
            "Not enough data available"
        }

        return data
    }
    val string get() = String(bytes, charset)
    val int get() = dataInputStream.readInt()
    val boolean get() = dataInputStream.readBoolean()
    val float get() = dataInputStream.readFloat()
    val double get() = dataInputStream.readDouble()
    val long get() = dataInputStream.readLong()
    val short get() = dataInputStream.readShort()
}
