package nl.pvdberg.pnet.client.util;

import nl.pvdberg.pnet.server.util.TLSServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.Assert.*;

public class TLSClientTest extends PlainClientTest
{
    protected static final String keyStoreFile = "testKeyStore.p12";
    protected static final String trustStoreFile = "testTrustStore.p12";
    protected static final char[] password = "password".toCharArray();
    protected static final String certType = "PKCS12";

    @Before
    @Override
    public void setUp() throws Exception
    {
        assertTrue(new File(keyStoreFile).exists());
        assertTrue(new File(trustStoreFile).exists());

        server = new TLSServer(
                new FileInputStream(keyStoreFile),
                password,
                certType
        );

        assertTrue(server.start(port));

        client = new TLSClient(
                new FileInputStream(trustStoreFile),
                password,
                certType
        );
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
}