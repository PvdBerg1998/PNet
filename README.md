# PNet
PNet is an easy to use network library for Java 1.6 or higher.

## Features
  - Guaranteed data transfer using TCP
  - Safe transfer using TLS
  - Support for GZIP compression
  - Asynchronous
  - Completely thread safe
  - Optimized, see [benchmarks](https://github.com/PvdBerg1998/PNet/wiki/Benchmarks)
  - Universal logging using [SLF4J](https://www.slf4j.org/)
  
---
  
# Download
PNet can be downloaded from the [releases](https://github.com/PvdBerg1998/PNet/releases) page.
PNet is released under the [MIT license](LICENSE.md).

---
  
# How to use
- [Packets](#packets)
- [Building Packets](#building-packets)
- [Reading Packets](#reading-packets)
- [Creating a Server](#creating-a-server)
- [Creating a Client](#creating-a-client)
- [Extra Client functionality](#extra-client-functionality)
- [Using TLS](#using-tls)
- [Using compression](#using-compression)
- [Smarter Packet handling](#smarter-packet-handling)
- [Multithreading Note](#multithreading-note)

---
  
## Packets
All data is sent using a `Packet`. These Packets are immutable and contain the following fields:
1. PacketType (Request or Reply)
2. Packet ID
3. Data

PacketType and Packet ID can be used to identify incoming Packets.
*Normally, you should not be creating Packets yourself.*

## Building Packets
To create a Packet, use a `PacketBuilder`. This helper object contains several methods to allow easy creation of Packets.
```Java
Packet packet = new PacketBuilder(Packet.PacketType.Request)
                .withInt(99)
                .withString("abc")
                .withBoolean(true)
                .build();
```

## Reading Packets
Just like the `PacketBuilder`, there is a `PacketReader`.
```Java
PacketReader packetReader = new PacketReader(packet);
int i = packetReader.readInt();
```
**The data of the Packet must be read in the same order as it was written!**

---

## Creating a Server
There are 2 different Server implementations available: `PlainServer` and `TLSServer`.
```Java
Server server = new PlainServer();
```
```Java
Server server = new TLSServer();
```
To use a Server, you might want to add a `PNetListener` to catch events.
```Java
server.setListener(new PNetListener()
{
    @Override
    public void onConnect(final Client c)
    {
        // Hello client!
    }

    @Override
    public void onDisconnect(final Client c)
    {
        // Goodbye :(
    }

    @Override
    public void onReceive(final Packet p, final Client c) throws IOException
    {
        // Handle Packet here
    }
});
```
The Server can be started: `server.start(port)` and stopped `server.stop()`.

---

## Creating a Client
There are 2 different Client implementations available: `PlainClient` and `TLSClient`.
```Java
Client client = new PlainClient();
```
```Java
Client client = new TLSClient();
```
A client fires events just like a Server does.
```Java
client.setClientListener(new PNetListener()
{
    @Override
    public void onConnect(final Client c)
    {
        // Hello server!
    }

    @Override
    public void onDisconnect(final Client c)
    {
        // Farewell
    }

    @Override
    public void onReceive(final Packet p, final Client c) throws IOException
    {
        // Handle Packet?
    }
});
```
Use `connect(host, port)` to connect, and `client.close()` to disconnect.

## Extra Client functionality
PNet contains 2 classes which can simplify using Clients even more.
1. AsyncClient
2. AutoClient

Any Client implementation can be passed to add functionality to.

The `AsyncClient` adds asynchronous functionality to a Client.
```Java
AsyncClient asyncClient = new AsyncClient(new PlainClient());
asyncClient.connectAsync("localhost", 8080, new AsyncListener()
{
    @Override
    public void onCompletion(final boolean success)
    {
        // Success?
    }
});
asyncClient.sendAsync(packet, new AsyncListener()
{
    @Override
    public void onCompletion(final boolean success)
    {
        // Success?
    }
});
```

The `AutoClient` automatically connects to given host:port so you don't have to check if the Client is connected.
```Java
AutoClient autoClient = new AutoClient(new TLSClient(), "localhost", 8080);
```

These implementations can be stacked.
```Java
AsyncClient stackedClient = new AsyncClient(new AutoClient(new TLSClient(), "localhost", 8080));
```
By stacking these implementations, you now have an asynchronous automatically connecting Client.

**Note that order is important.**
```Java
AutoClient invalid = new AutoClient(new AsyncClient(new TLSClient()), "localhost", 8080);
```
In this example, the `AutoClient` will call the default `connect` and `send`, which will render the `AsyncClient` completely useless.
If used correctly, the `AsyncClient` will expose `connectAsync` and `sendAsync`, which will call `connect` and `sync` from the `AutoClient` asynchronously.

---

## Using TLS
When using `TLSServer`, a key store is required. When using `TLSClient`, a trust store is required.
These values can be set using the `TLS` helper class.
```Java
TLS.setKeyStore("keystore.jks", "password");
TLS.setTrustStore("truststore.ts", "password");
```
SSL debug output can be turned on by calling `TLS.setSSLDebug()`.

PNet is configured to use the latest, most secure TLS protocols and cipher suites available.

## Using compression
To compress a Packet, use the `PacketCompressor` helper class.
```Java
Packet compressed = PacketCompressor.compress(packet);
Packet decompressed = PacketCompressor.decompress(packet);
```

---

## Smarter Packet handling
A common mistake people make is to handle all different Packets in the main `onReceive` event handler. When handling various Packets with various Packet IDs, make sure to use a `PacketDistributer`. A `pNetListener` implementation is available to link your distributer to your Client or Server.
```Java
PacketDistributer packetDistributer = new PacketDistributer();
client.setClientListener(new DistributerListener(packetDistributer));
server.setListener(new DistributerListener(packetDistributer));
```
For each Packet ID you want to handle, add a `PacketHandler` implementation.
```Java
short someID = 666;
packetDistributer.addHandler(someID, new PacketHandler()
{
    @Override
    public void handlePacket(final Packet p, final Client c) throws IOException
    {
        // Handle this evil Packet for me please
    }
});
```
Even better: separate all the handlers into their own class.
```Java
short anotherID = 123;
packetDistributer.addHandler(anotherID, new anotherHandlerClass());
```
A default handler can be set by using `packetDistributer.setDefaultHandler(PacketHandler)`.

---

## Multithreading Note
PNet uses a threadpool to handle all threading. If your application needs to shut down immediately, this can be done by killing all threads using `ThreadManager.shutdown()`.
