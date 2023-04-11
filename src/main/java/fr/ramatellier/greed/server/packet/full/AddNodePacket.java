package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;

import java.nio.ByteBuffer;

public record AddNodePacket(IDPacket src, IDPacket daughter) implements BroadcastPacket {
    @Override
    public OpCodes opCode() {
        return OpCodes.ADD_NODE;
    }

    @Override
    public void put(ByteBuffer buffer) {
        src.putInBuffer(buffer);
        daughter.putInBuffer(buffer);
    }

    @Override
    public int size() {
        return Byte.BYTES * 2 + src.size() + daughter.size();
    }

    @Override
    public BroadcastPacket withNewSource(IDPacket newSrc) {
        return new AddNodePacket(newSrc, daughter);
    }
}
