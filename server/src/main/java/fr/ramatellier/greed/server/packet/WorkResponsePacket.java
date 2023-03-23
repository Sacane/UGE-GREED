package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;

public final class WorkResponsePacket implements FullPacket{

    public WorkResponsePacket(IDPacket src, IDPacket dst, long requestID, ResponsePacket responsePacket) {

    }
    @Override
    public TramKind kind() {
        return TramKind.TRANSFERT;
    }

    @Override
    public byte opCode() {
        return OpCodes.WORK_RESPONSE;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {

    }
}
