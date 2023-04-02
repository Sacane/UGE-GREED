package fr.ramatellier.greed.server.reader.primitive;

import fr.ramatellier.greed.server.reader.Reader;

import java.nio.ByteBuffer;

public class LongReader extends PrimitiveReader<Long> implements Reader<Long> {
    public LongReader() {
        super(Long.BYTES, ByteBuffer::getLong);
    }
}
