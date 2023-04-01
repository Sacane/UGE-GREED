package fr.ramatellier.greed.server.packet.sub;

import fr.ramatellier.greed.server.packet.Packet;
import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;

public record PacketHeader(TramKind kind, byte opCode) implements Packet {
    @Override
    public void putInBuffer(ByteBuffer buffer) {
        buffer.put(kind.BYTES);
        buffer.put(opCode);
    }
}