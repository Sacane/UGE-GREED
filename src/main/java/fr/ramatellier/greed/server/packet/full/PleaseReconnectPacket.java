package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public final class PleaseReconnectPacket implements LocalPacket {
    private final IDPacket id;

    public PleaseReconnectPacket(InetSocketAddress address) {
        id = new IDPacket(address);
    }

    public IDPacket getId() {
        return id;
    }

    @Override
    public OpCodes opCode() {
        return OpCodes.PLEASE_RECONNECT;
    }

    @Override
    public void put(ByteBuffer buffer) {
        id.putInBuffer(buffer);
    }

    @Override
    public int size() {
        return Byte.BYTES * 2 + id.size();
    }
}
