package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;
import java.util.Objects;

public record WorkRequestResponsePacket(IDPacket dst, IDPacket src, long requestID, long nb_uc) implements FullPacket {

    public WorkRequestResponsePacket{
        Objects.requireNonNull(dst);
        Objects.requireNonNull(src);
    }
    @Override
    public TramKind kind() {
        return TramKind.TRANSFER;
    }

    @Override
    public byte opCode() {
        return OpCodes.WORK_REQUEST_RESPONSE.BYTES;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        putHeader(buffer);
        dst.putInBuffer(buffer);
        src.putInBuffer(buffer);
        buffer.putLong(requestID);
        buffer.putLong(nb_uc);
    }

}
