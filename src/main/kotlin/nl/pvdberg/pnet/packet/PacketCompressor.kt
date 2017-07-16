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
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object PacketCompressor
{
    val INFLATE_BUFFER_SIZE = 128

    /**
     * Decompresses given Packet
     * @param packet Compressed Packet
     * @return Decompressed Packet
     */
    fun decompress(packet: Packet): Packet
    {
        val gzipInputStream = GZIPInputStream(ByteArrayInputStream(packet.data))
        val byteArrayOutputStream = ByteArrayOutputStream()

        // Read from input until everything is inflated
        val buffer = ByteArray(INFLATE_BUFFER_SIZE)
        do
        {
            val bytesInflated = gzipInputStream.read(buffer)
            byteArrayOutputStream.write(buffer, 0, bytesInflated)
        } while (bytesInflated > 0)

        return Packet(
                packet.packetType,
                packet.packetID,
                byteArrayOutputStream.toByteArray()
        )
    }

    /**
     * Compresses given Packet
     * @param packet Packet to compress
     * @return Compressed Packet
     */
    fun compress(packet: Packet): Packet
    {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val gzipOutputStream = object : GZIPOutputStream(byteArrayOutputStream)
        {
            init
            {
                def.setLevel(Deflater.BEST_COMPRESSION)
            }
        }

        // Deflate all data
        gzipOutputStream.write(packet.data)
        gzipOutputStream.close()

        return Packet(
                packet.packetType,
                packet.packetID,
                byteArrayOutputStream.toByteArray()
        )
    }
}
