package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.packet.sub.RangePacket;
import fr.ramatellier.greed.server.util.OpCodes;

import java.nio.ByteBuffer;

public record WorkAssignmentPacket(IDPacket src, IDPacket dst, Long requestId, RangePacket range) implements TransferPacket {

    @Override
    public OpCodes opCode() {
        return OpCodes.WORK_ASSIGNMENT;
    }

    @Override
    public void put(ByteBuffer buffer) {
        src.putInBuffer(buffer);
        dst.putInBuffer(buffer);
        buffer.putLong(requestId);
        range.putInBuffer(buffer);
    }

    @Override
    public int size() {
        return Byte.BYTES * 2 + src.size() + dst.size() + Long.BYTES + range.size();
    }
}
