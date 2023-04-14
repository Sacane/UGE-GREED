package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.packet.sub.ResponsePacket;
import fr.ramatellier.greed.server.util.OpCodes;

import java.nio.ByteBuffer;
import java.util.Objects;

public record WorkResponsePacket(
    IDPacket src,
    IDPacket dst,
    Long requestID,
    ResponsePacket responsePacket
) implements TransferPacket {
    public WorkResponsePacket{
        Objects.requireNonNull(src);
        Objects.requireNonNull(dst);
        Objects.requireNonNull(responsePacket);
    }

    @Override
    public OpCodes opCode() {
        return OpCodes.WORK_RESPONSE;
    }

    @Override
    public void put(ByteBuffer buffer) {
        src.putInBuffer(buffer);
        dst.putInBuffer(buffer);
        buffer.putLong(requestID);
        responsePacket.putInBuffer(buffer);
    }

    @Override
    public int size() {
        return Byte.BYTES * 2 + Long.BYTES + src.size() + dst.size() + responsePacket.size();
    }

    public String result(){
        return responsePacket.getResponse().value();
    }
}
