package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.util.OpCodes;

import java.nio.ByteBuffer;

public record LogoutDeniedPacket() implements LocalPacket {
    @Override
    public OpCodes opCode() {
        return OpCodes.LOGOUT_DENIED;
    }

    @Override
    public void put(ByteBuffer buffer) {

    }

    @Override
    public int size() {
        return Byte.BYTES * 2;
    }
}