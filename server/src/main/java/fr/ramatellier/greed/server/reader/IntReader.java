package fr.ramatellier.greed.server.reader;

import java.nio.ByteBuffer;

public class IntReader extends PrimitiveReader<Integer> implements Reader<Integer> {
    public IntReader() {
        super(Integer.BYTES, ByteBuffer::getInt);
    }
}