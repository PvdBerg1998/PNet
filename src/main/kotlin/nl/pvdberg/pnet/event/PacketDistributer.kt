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

package nl.pvdberg.pnet.event

import nl.pvdberg.pnet.client.Client
import nl.pvdberg.pnet.packet.Packet

import java.io.IOException
import java.util.HashMap

class PacketDistributer
{
    /**
     * Returns global handler
     * @return Packet Distributer (may be null)
     */
    /**
     * Sets handler which receives all events
     * @param globalHandler Nullable Packet Distributer
     */
    @get:Synchronized @set:Synchronized var globalHandler: PacketDistributer? = null
    private var defaultHandler: PacketHandler? = null
    private val registry: MutableMap<Short, PacketHandler>

    /**
     * Creates a new Packet Distributer. Use this to link functionality to different Packet IDs
     */
    init
    {
        registry = HashMap<Short, PacketHandler>()
    }

    /**
     * Calls specified handler for Packet ID, or the default handler (if set)
     * @param packet New incoming Packet
     */
    @Synchronized @Throws(IOException::class)
    fun onReceive(packet: Packet, client: Client)
    {
        if (globalHandler != null) globalHandler!!.onReceive(packet, client)

        val packetHandler = registry[packet.packetID]
        if (packetHandler == null)
        {
            if (defaultHandler != null) defaultHandler!!.handlePacket(packet, client)
        }
        else
            packetHandler.handlePacket(packet, client)
    }

    /**
     * Adds a new handler for this specific Packet ID
     * @param packetID Packet ID to add handler for
     * *
     * @param packetHandler Handler for given Packet ID
     * *
     * @throws IllegalArgumentException when Packet ID already has a registered handler
     */
    @Synchronized fun addHandler(packetID: Short, packetHandler: PacketHandler)
    {
        if (registry.containsKey(packetID)) throw IllegalArgumentException("Handler for ID: $packetID already exists")
        registry.put(packetID, packetHandler)
    }

    /**
     * Returns PacketHandler associated with given Packet ID
     * @param packetID Packet ID
     * *
     * @return PacketHandler or null if no PacketHandler is found
     */
    @Synchronized fun getHandler(packetID: Short): PacketHandler
    {
        return registry[packetID]
    }

    /**
     * Removes all registered handlers except the default handler.
     * Removing the default handler can be done by calling [setDefaultHandler(null)][.setDefaultHandler]
     */
    @Synchronized fun clearHandlers()
    {
        registry.clear()
    }

    /**
     * Sets handler for Packet ID's without custom handler
     * @param defaultHandler Nullable Packet handler
     */
    @Synchronized fun setDefaultHandler(defaultHandler: PacketHandler)
    {
        this.defaultHandler = defaultHandler
    }
}
