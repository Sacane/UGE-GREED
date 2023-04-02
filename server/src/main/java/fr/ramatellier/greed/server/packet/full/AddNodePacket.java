package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;

import java.nio.ByteBuffer;


public record AddNodePacket(IDPacket src, IDPacket daughter) implements FullPacket, BroadcastPacket {

    @Override
    public OpCodes opCode() {
        return OpCodes.ADD_NODE;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        putHeader(buffer);
        src.putInBuffer(buffer);
        daughter.putInBuffer(buffer);
    }
}
