package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;

public record BroadcastOnePacket(IDPacket src) implements BroadcastPacket{
    @Override
    public void putInBuffer(ByteBuffer buffer) {

    }

    @Override
    public TramKind kind() {
        return null;
    }

    @Override
    public OpCodes opCode() {
        return null;
    }
}
