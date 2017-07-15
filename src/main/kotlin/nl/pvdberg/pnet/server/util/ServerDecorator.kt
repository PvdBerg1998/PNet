package nl.pvdberg.pnet.server.util

import nl.pvdberg.pnet.event.PNetListener
import nl.pvdberg.pnet.server.Server

open class ServerDecorator(protected val server: Server) : Server
{

    override fun setListener(serverListener: PNetListener)
    {
        server.setListener(serverListener)
    }

    override fun start(port: Int): Boolean
    {
        return server.start(port)
    }

    override fun stop()
    {
        server.stop()
    }
}
