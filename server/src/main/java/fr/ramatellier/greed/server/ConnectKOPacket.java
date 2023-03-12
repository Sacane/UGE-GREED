package fr.ramatellier.greed.server;

import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Objects;

public class ConnectKOPacket implements Packet{

    private final String localAddress;
    public ConnectKOPacket(String address) {
        this.localAddress = Objects.requireNonNull(address);
    }
    @Override
    public void putInBuffer(ByteBuffer buffer) {
        buffer.put(TramKind.LOCAL.bytes);
        buffer.put(OpCodes.KO);
        var ipPacket = new IPPacket(localAddress);
        ipPacket.putInBuffer(buffer);
    }
}
