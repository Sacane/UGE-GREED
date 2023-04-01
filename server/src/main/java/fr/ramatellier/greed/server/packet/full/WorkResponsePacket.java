package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.ResponsePacket;
import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;
import java.util.Objects;

public record WorkResponsePacket(
    IDPacket src,
    IDPacket dst,
    long requestID,
    ResponsePacket responsePacket
) implements FullPacket {
    public WorkResponsePacket{
        Objects.requireNonNull(src);
        Objects.requireNonNull(dst);
        Objects.requireNonNull(responsePacket);
    }
    @Override
    public TramKind kind() {
        return TramKind.TRANSFER;
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

    public IDPacket src() {
        return src;
    }

    public IDPacket dst() {
        return dst;
    }
}
