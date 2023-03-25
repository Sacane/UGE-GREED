package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;
import java.util.Objects;

public record WorkResponsePacket(
    IDPacket src,
    IDPacket dst,
    long requestID,
    ResponsePacket responsePacket
) implements FullPacket{
    public WorkResponsePacket{
        Objects.requireNonNull(src);
        Objects.requireNonNull(dst);
        Objects.requireNonNull(responsePacket);
    }
    @Override
    public TramKind kind() {
        return TramKind.TRANSFERT;
    }

    @Override
    public byte opCode() {
        return OpCodes.WORK_RESPONSE.BYTES;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        putHeader(buffer);
        src.putInBuffer(buffer);
        dst.putInBuffer(buffer);
        responsePacket.putInBuffer(buffer);
    }
}
