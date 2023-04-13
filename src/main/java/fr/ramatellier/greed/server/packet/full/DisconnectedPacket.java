package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;

import java.nio.ByteBuffer;

public record DisconnectedPacket(IDPacket src, IDPacket id) implements BroadcastPacket {


    @Override
    public BroadcastPacket withNewSource(IDPacket newSrc) {
        return new DisconnectedPacket(newSrc, id);
    }

    @Override
    public OpCodes opCode() {
        return OpCodes.DISCONNECTED;
    }

    @Override
    public void put(ByteBuffer buffer) {
        src.putInBuffer(buffer);
        id.putInBuffer(buffer);
    }

    @Override
    public int size() {
        return Byte.BYTES * 2 + src.size() + id.size();
    }
}
