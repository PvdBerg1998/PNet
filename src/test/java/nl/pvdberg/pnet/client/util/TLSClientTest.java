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

package nl.pvdberg.pnet.client.util;

import nl.pvdberg.pnet.client.Client;
import nl.pvdberg.pnet.event.PNetListener;
import nl.pvdberg.pnet.packet.Packet;
import nl.pvdberg.pnet.server.util.TLSServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertTrue;

public class TLSClientTest extends PlainClientTest
{
    protected static final File keyStoreFile = new File("testKeyStore.p12");
    protected static final File trustStoreFile = new File("testTrustStore.p12");
    protected static final char[] password = "password".toCharArray();
    protected static final String certType = "PKCS12";

    @Before
    @Override
    public void setUp() throws Exception
    {
        assertTrue(keyStoreFile.exists());
        assertTrue(trustStoreFile.exists());

        server = new TLSServer(
                fileToBytes(keyStoreFile),
                password,
                certType
        );

        assertTrue(server.start(port));

        client = new TLSClient(
                fileToBytes(trustStoreFile),
                password,
                certType
        );
    }

    private static byte[] fileToBytes(final File file)
    {
        final FileInputStream fileInputStream;

        final byte[] data = new byte[(int) file.length()];
        try
        {
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(data);
            fileInputStream.close();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }

        return data;
    }

    @After
    @Override
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    @Test
    @Override
    public void connect() throws Exception
    {
        super.connect();
    }

    @Test
    @Override
    public void nonConnectedSend() throws Exception
    {
        super.nonConnectedSend();
    }

    @Test
    @Override
    public void send() throws Exception
    {
        super.send();
    }

    @Test
    @Override
    public void clientType() throws Exception
    {
        client.setClientListener(new PNetListener()
        {
            @Override
            public void onConnect(final Client c)
            {
                assertTrue(c instanceof TLSClient);
            }

            @Override
            public void onDisconnect(final Client c)
            {

            }

            @Override
            public void onReceive(final Packet p, final Client c) throws IOException
            {

            }
        });

        assertTrue(client.connect("localhost", port));
    }
}