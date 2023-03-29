package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;

public record WorkRequestResponse(IDPacket dst, IDPacket src, long requestID, long nb_uc) implements FullPacket{
    @Override
    public TramKind kind() {
        return TramKind.TRANSFERT;
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
