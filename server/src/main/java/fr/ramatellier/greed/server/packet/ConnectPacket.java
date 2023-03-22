package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public final class ConnectPacket implements FullPacket {
    private final IDPacket idPacket;

    public ConnectPacket(InetSocketAddress address) {
        idPacket = new IDPacket(address);
    }

    public String getAddress() {
        return idPacket.getAddress();
    }

    public int getPort() {
        return idPacket.getPort();
    }

    public InetSocketAddress getSocket() {
        return idPacket.getSocket();
    }

    @Override
    public TramKind kind() {
        return TramKind.LOCAL;
    }

    @Override
    public byte opCode() {
        return OpCodes.CONNECT;
    }

    public void putInBuffer(ByteBuffer buffer) {
        buffer.put(kind().BYTES);
        buffer.put((byte) 1);
        idPacket.putInBuffer(buffer);
    }
}
