package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.Packet;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public interface Reader<T> {
    enum ProcessStatus {
        DONE, REFILL, ERROR
    }
    ProcessStatus process(ByteBuffer buffer);
    T get();
    void reset();

    default <E extends Packet> void fillList(ArrayList<E> list, int size, Reader<E> reader, ByteBuffer buffer) {
        while(buffer.limit() > 0 && list.size() != size) {
            var status = reader.process(buffer);

            if(status == ProcessStatus.DONE) {
                list.add(reader.get());
                reader.reset();
            }
        }
    }

    default void fillBuffer(ByteBuffer srcBuffer, ByteBuffer dstBuffer) {
        try {
            srcBuffer.flip();

            if (srcBuffer.remaining() <= dstBuffer.remaining()) {
                dstBuffer.put(srcBuffer);
            } else {
                var oldLimit = srcBuffer.limit();
                srcBuffer.limit(srcBuffer.position() + dstBuffer.remaining());
                dstBuffer.put(srcBuffer);
                srcBuffer.limit(oldLimit);
            }
        } finally {
            srcBuffer.compact();
        }
    }
}
