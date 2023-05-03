package fr.ramatellier.greed.server.frame.component;

import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.component.RangeComponentReader;

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

    @Override
    public Reader<? extends GreedComponent> reader() {
        return new RangeComponentReader();
    }
}
