package fr.ramatellier.greed.server.model.component;

import java.nio.ByteBuffer;

public record RangeComponent(long start, long end) implements GreedComponent {

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
