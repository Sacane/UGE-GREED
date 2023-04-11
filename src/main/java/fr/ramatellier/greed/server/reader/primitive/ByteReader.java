package fr.ramatellier.greed.server.reader.primitive;

import fr.ramatellier.greed.server.reader.Reader;

import java.nio.ByteBuffer;

public class ByteReader extends PrimitiveReader<Byte> implements Reader<Byte> {
    public ByteReader() {
        super(Byte.BYTES, ByteBuffer::get);
    }
}
