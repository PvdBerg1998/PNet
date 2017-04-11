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

package test.nl.pvdberg.pnet.packet;

import main.nl.pvdberg.pnet.packet.Packet;
import main.nl.pvdberg.pnet.packet.PacketBuilder;
import main.nl.pvdberg.pnet.packet.PacketCompressor;
import org.junit.Test;

import java.util.Random;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

public class PacketCompressorTest
{
    @Test
    public void compressAndDecompress() throws Exception
    {
        final byte[] data = new byte[8096];
        new Random().nextBytes(data);

        final Packet packet = new PacketBuilder(Packet.PacketType.Reply)
                .withBytes(data)
                .build();

        final Packet compressed = PacketCompressor.compress(packet);
        final Packet decompressed = PacketCompressor.decompress(compressed);

        assertThat(compressed.getData(), not(equalTo(data)));
        assertArrayEquals(data, decompressed.getData());
    }
}