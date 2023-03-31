package fr.ramatellier.greed.server.packet;

import java.nio.ByteBuffer;

public record RangePacket(long start, long end) implements Packet {

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        buffer.putLong(start);
        buffer.putLong(end);
    }
}
