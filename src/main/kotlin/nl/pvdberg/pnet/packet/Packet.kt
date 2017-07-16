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

import nl.pvdberg.hashkode.compareFields
import nl.pvdberg.hashkode.hashKode
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*

data class Packet(
        val packetType: PacketType,
        val packetID: Short,
        val data: ByteArray
)
{
    enum class PacketType
    {
        Request,
        Reply;
    }

    val isRequest inline get() = packetType == PacketType.Request
    val isReply inline get () = packetType == PacketType.Reply

    /**
     * Writes Packet into DataOutputStream
     * @param out DataOutputStream to write into
     */
    fun write(out: DataOutputStream) = with(out)
    {
        writeByte(packetType.ordinal)
        writeShort(packetID.toInt())
        writeInt(data.size)
        write(data)
    }

    override fun equals(other: Any?) = compareFields(other)
    {
        compareField(Packet::packetType)
        compareField(Packet::packetID)
        compareBy { Arrays.equals(one.data, two.data) }
    }

    override fun hashCode() = hashKode(packetType, packetID, data)

    companion object
    {
        val DEFAULT_CHARSET = Charsets.UTF_8
        private val fastValues = PacketType.values()

        /**
         * Reads a Packet from raw input data
         * @param in DataInputStream to fromStream from
         * @return Packet created from input
         */
        fun fromStream(inputStream: DataInputStream) =
                Packet(
                        packetType = fastValues[inputStream.readByte().toInt()],
                        packetID = inputStream.readShort(),
                        data = ByteArray(inputStream.readInt()).apply { inputStream.readFully(this) }
                )
    }
}
