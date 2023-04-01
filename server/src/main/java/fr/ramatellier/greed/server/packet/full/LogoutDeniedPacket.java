package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.full.FullPacket;
import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;

public final class LogoutDeniedPacket implements FullPacket {
    @Override
    public TramKind kind() {
        return TramKind.LOCAL;
    }

    @Override
    public byte opCode() {
        return OpCodes.LOGOUT_DENIED.BYTES;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        putHeader(buffer);
    }
}
