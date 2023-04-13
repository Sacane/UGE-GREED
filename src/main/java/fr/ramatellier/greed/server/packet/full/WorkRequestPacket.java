package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.LongPacketPart;
import fr.ramatellier.greed.server.packet.sub.RangePacket;
import fr.ramatellier.greed.server.packet.sub.CheckerPacket;
import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;

import java.nio.ByteBuffer;

public record WorkRequestPacket(IDPacket src, IDPacket dst, LongPacketPart requestId, CheckerPacket checker, RangePacket range, LongPacketPart max) implements TransferPacket {

    @Override
    public OpCodes opCode() {
        return OpCodes.WORK;
    }

    @Override
    public void put(ByteBuffer buffer) {
        src.putInBuffer(buffer);
        dst.putInBuffer(buffer);
        buffer.putLong(requestId.get());
        checker.putInBuffer(buffer);
        range.putInBuffer(buffer);
        buffer.putLong(max.get());
    }

    @Override
    public int size() {
        return Byte.BYTES * 2 + src.size() + dst.size() + Long.BYTES * 2 + checker.size() + range.size();
    }
}
