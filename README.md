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
Normally, you should **not** be creating Packets yourself.

## Building Packets
To create a Packet, use a `PacketBuilder`. This helper object contains several functions to allow easy creation of Packets.
```Java
Packet packet = new PacketBuilder(Packet.PacketType.Request)
                .withInt(99)
                .withString("abc")
                .withBoolean(true)
                .build();
```

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

## Creating a Client

## Extra Client functionality

## Using TLS

## Using compression
