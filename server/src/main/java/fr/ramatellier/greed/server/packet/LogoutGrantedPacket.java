package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;

public final class LogoutGrantedPacket implements FullPacket {
    public LogoutGrantedPacket() {
    }

    @Override
    public TramKind kind() {
        return TramKind.LOCAL;
    }

    @Override
    public byte opCode() {
        return OpCodes.LOGOUT_GRANTED.BYTES;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        putHeader(buffer);
    }
}
