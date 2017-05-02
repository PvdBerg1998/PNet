package nl.pvdberg.pnet.server.util;

import nl.pvdberg.pnet.event.PNetListener;
import nl.pvdberg.pnet.server.Server;

public class ServerDecorator implements Server
{
    protected final Server server;

    public ServerDecorator(final Server server)
    {
        this.server = server;
    }

    @Override
    public void setListener(final PNetListener serverListener)
    {
        server.setListener(serverListener);
    }

    @Override
    public boolean start(final int port)
    {
        return server.start(port);
    }

    @Override
    public void stop()
    {
        server.stop();
    }
}
