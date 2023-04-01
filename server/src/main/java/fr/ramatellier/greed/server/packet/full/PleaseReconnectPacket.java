package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public final class PleaseReconnectPacket implements FullPacket {
    private final IDPacket id;

    public PleaseReconnectPacket(InetSocketAddress address) {
        id = new IDPacket(address);
    }

    public IDPacket getId() {
        return id;
    }

    @Override
    public TramKind kind() {
        return TramKind.LOCAL;
    }

    @Override
    public byte opCode() {
        return OpCodes.PLEASE_RECONNECT.BYTES;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        putHeader(buffer);
        id.putInBuffer(buffer);
    }
}
