package fr.ramatellier.greed.server.reader.primitive;

import fr.ramatellier.greed.server.reader.Reader;

import java.nio.ByteBuffer;

public class IntReader extends PrimitiveReader<Integer> implements Reader<Integer> {
    public IntReader() {
        super(Integer.BYTES, ByteBuffer::getInt);
    }
}