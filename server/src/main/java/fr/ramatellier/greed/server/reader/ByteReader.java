package fr.ramatellier.greed.server.reader;

import java.nio.ByteBuffer;

public class ByteReader extends PrimitiveReader<Byte> implements Reader<Byte> {
    public ByteReader() {
        super(Byte.BYTES, ByteBuffer::get);
    }
}
