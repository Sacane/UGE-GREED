package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.util.OpCodes;

import java.nio.ByteBuffer;

public final class LogoutDeniedPacket implements LocalPacket, FullPacket {

    @Override
    public OpCodes opCode() {
        return OpCodes.LOGOUT_DENIED;
    }


    @Override
    public void putInBuffer(ByteBuffer buffer) {
        putHeader(buffer);
    }
}
