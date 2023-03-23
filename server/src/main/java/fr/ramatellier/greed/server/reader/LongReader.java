package fr.ramatellier.greed.server.reader;

import java.nio.ByteBuffer;

public class LongReader extends PrimitiveReader<Long> {
    public LongReader() {
        super(Long.BYTES, ByteBuffer::getLong);
    }
}
