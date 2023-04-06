package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public final class ConnectPacket implements FullPacket, LocalPacket {
    private final IDPacket idPacket;

    public ConnectPacket(InetSocketAddress address) {
        idPacket = new IDPacket(address);
    }

    public String getAddress() {
        return idPacket.getHostname();
    }

    public int getPort() {
        return idPacket.getPort();
    }

    public InetSocketAddress getSocket() {
        return idPacket.getSocket();
    }

    @Override
    public OpCodes opCode() {
        return OpCodes.CONNECT;
    }

    public void putInBuffer(ByteBuffer buffer) {
        putHeader(buffer);
        idPacket.putInBuffer(buffer);
    }

    @Override
    public int size() {
        return Byte.BYTES * 2 + idPacket.size();
    }
}
