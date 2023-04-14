package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;

import java.nio.ByteBuffer;

public record PleaseReconnectPacket(IDPacket id) implements LocalPacket {

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
