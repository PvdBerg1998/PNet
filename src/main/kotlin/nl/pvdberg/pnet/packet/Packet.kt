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

import java.io.*

class Packet
/**
 * Creates a new immutable Packet
 * @param packetType Packet Type
 * *
 * @param packetID Packet ID
 * *
 * @param data Packet Data
 */
(
        /**
         * Returns Packet Type
         * @return Packet Type
         */
        val packetType: PacketType,
        /**
         * Returns Packet ID
         * @return Packet ID
         */
        val packetID: Short,
        /**
         * Returns Packet data
         * @return Data
         */
        val data: ByteArray)
{
    /**
     * Returns Data length
     * @return Data length
     */
    val dataLength: Int

    enum class PacketType
    {
        Request,
        Reply;

        companion object
        {

            val fastValues = values()
        }
    }

    init
    {
        dataLength = data.size
    }

    /**
     * Returns whether Packet is of type Request
     * @return PacketType is Request
     */
    val isRequest: Boolean
        get() = packetType == PacketType.Request

    /**
     * Returns whether Packet is of type Reply
     * @return PacketType is Reply
     */
    val isReply: Boolean
        get() = packetType == PacketType.Reply

    /**
     * Writes Packet into DataOutputStream
     * @param out DataOutputStream to write into
     * *
     * @throws IOException when unable to write to stream
     */
    @Throws(IOException::class)
    fun write(out: DataOutputStream)
    {
        // Packet Type
        out.writeByte(packetType.ordinal)

        // Packet ID
        out.writeShort(packetID.toInt())

        // Data Length
        out.writeInt(dataLength)

        // Data
        out.write(data)
    }

    override fun toString(): String
    {
        return "Type: [$packetType] ID: [$packetID] Data: [$dataLength bytes]"
    }

    companion object
    {

        /**
         * Reads a Packet from raw input data
         * @param in DataInputStream to fromStream from
         * *
         * @return Packet created from input
         * *
         * @throws IOException when unable to read from stream
         */
        @Throws(IOException::class)
        fun fromStream(`in`: DataInputStream): Packet
        {
            // Packet Type
            val packetType = Packet.PacketType.fastValues[`in`.readByte()]

            // Packet ID
            val packetID = `in`.readShort()

            // Data Length
            val dataLength = `in`.readInt()

            // Data
            val data = ByteArray(dataLength)
            `in`.readFully(data)

            return Packet(
                    packetType,
                    packetID,
                    data
            )
        }
    }
}
