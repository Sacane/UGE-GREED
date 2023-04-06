package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.full.FullPacket;
import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;

public final class LogoutGrantedPacket implements LocalPacket {
    @Override
    public OpCodes opCode() {
        return OpCodes.LOGOUT_GRANTED;
    }

    @Override
    public void put(ByteBuffer buffer) {}

    @Override
    public int size() {
        return Byte.BYTES * 2;
    }
}
