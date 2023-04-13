package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.packet.sub.LongPacketPart;
import fr.ramatellier.greed.server.util.OpCodes;

import java.nio.ByteBuffer;
import java.util.Objects;

public record WorkRequestResponsePacket(IDPacket dst, IDPacket src, LongPacketPart requestID, LongPacketPart nb_uc) implements TransferPacket {

    public WorkRequestResponsePacket{
        Objects.requireNonNull(dst);
        Objects.requireNonNull(src);
    }

    @Override
    public OpCodes opCode() {
        return OpCodes.WORK_REQUEST_RESPONSE;
    }

    @Override
    public void put(ByteBuffer buffer) {
        dst.putInBuffer(buffer);
        src.putInBuffer(buffer);
        buffer.putLong(requestID.get());
        buffer.putLong(nb_uc.get());
    }

    @Override
    public int size() {
        return Byte.BYTES * 2 + Long.BYTES * 2 + dst.size() + src.size();
    }

}
