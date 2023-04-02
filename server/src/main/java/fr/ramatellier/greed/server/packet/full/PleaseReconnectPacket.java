package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public final class PleaseReconnectPacket implements FullPacket, LocalPacket {
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
    public void putInBuffer(ByteBuffer buffer) {
        putHeader(buffer);
        id.putInBuffer(buffer);
    }
}
