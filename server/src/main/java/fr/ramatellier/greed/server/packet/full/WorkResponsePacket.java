package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.ResponsePacket;
import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;
import java.nio.ByteBuffer;
import java.util.Objects;

public record WorkResponsePacket(
    IDPacket src,
    IDPacket dst,
    long requestID,
    ResponsePacket responsePacket
) implements FullPacket, TransferPacket {
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
    public void putInBuffer(ByteBuffer buffer) {
        putHeader(buffer);
        src.putInBuffer(buffer);
        dst.putInBuffer(buffer);
        buffer.putLong(requestID);
        responsePacket.putInBuffer(buffer);
    }

    @Override
    public int size() {
        return 0;
    }

    public IDPacket src() {
        return src;
    }

    public IDPacket dst() {
        return dst;
    }
}
