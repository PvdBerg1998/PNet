# PNet
PNet is an easy to use networking framework for Java.

## Features
  - Guaranteed data transfer using TCP
  - Safe transfer using TLS
  - Support for GZIP compression
  - Asynchronous
  - Completely thread safe
  - Optimized
  
## Packets
All data is sent using a `Packet`. These Packets are immutable and contain the following fields:
1. PacketType (Request or Reply)
2. Packet ID
3. Data

PacketType and Packet ID can be used to identify incoming Packets.
*Normally, you should not be creating Packets yourself.*

## Building Packets
To create a Packet, use a `PacketBuilder`. This helper object contains several functions to allow easy creation of Packets.
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
After this, the Server can be started.
```Java
server.start(port);
```
Ofcourse, the Server can also be stopped.
```Java
server.stop();
```

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
To connect, call the obvious method.
```Java
client.connect("localhost", 8080);
```
The same goes for closing the connection:
```Java
client.close();
```

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

*These implementations can be stacked!*
```Java
AsyncClient stackedClient = new AsyncClient(new AutoClient(new TLSClient(), "localhost", 8080));
```
By stacking these implementations, you now have an asynchronous automatically connecting Client.
Note that order is important.
```Java
AutoClient invalid = new AutoClient(new AsyncClient(new TLSClient()), "localhost", 8080);
```
In this example, the `AutoClient` will call the default `connect` and `send`, which will render the `AsyncClient` completely useless.
If used correctly, the `AsyncClient` will expose `connectAsync` and `sendAsync`, which will call `connect` and `sync` from the `AutoClient` asynchronously.

## Using TLS

## Using compression
