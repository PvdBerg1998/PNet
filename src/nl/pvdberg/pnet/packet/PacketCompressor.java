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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PacketCompressor
{
    private static final int INFLATE_BUFFER_SIZE = 16;

    /**
     * Decompresses given Packet
     * @param packet Compressed Packet
     * @return Decompressed Packet
     * @throws IOException when unable to decompress
     */
    public static Packet decompress(final Packet packet) throws IOException
    {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(packet.getData());
        final GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Read from input until everything is inflated
        final byte buffer[] = new byte[INFLATE_BUFFER_SIZE];
        int bytesInflated;
        while ((bytesInflated = gzipInputStream.read(buffer)) >= 0)
        {
            byteArrayOutputStream.write(buffer, 0, bytesInflated);
        }

        return new Packet(
                packet.getPacketType(),
                packet.getPacketID(),
                byteArrayOutputStream.toByteArray()
        );
    }

    /**
     * Compresses given Packet. Note that this can increase the total size when used incorrectly
     * @param packet Packet to compress
     * @return Compressed Packet
     * @throws IOException when unable to compress
     */
    public static Packet compress(final Packet packet) throws IOException
    {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)
        {
            {
                def.setLevel(Deflater.BEST_COMPRESSION);
            }
        };

        // Deflate all data
        gzipOutputStream.write(packet.getData());
        gzipOutputStream.close();

        return new Packet(
                packet.getPacketType(),
                packet.getPacketID(),
                byteArrayOutputStream.toByteArray()
        );
    }
}
