package fr.ramatellier.greed.server.packet.sub;

import fr.ramatellier.greed.server.packet.GreedComponent;

import java.nio.ByteBuffer;

public record RangePacket(long start, long end) implements GreedComponent {

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        buffer.putLong(start);
        buffer.putLong(end);
    }

    @Override
    public int size() {
        return Long.BYTES * 2;
    }
}
