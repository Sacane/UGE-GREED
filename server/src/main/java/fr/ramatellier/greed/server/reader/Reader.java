package fr.ramatellier.greed.server.reader;

import java.nio.ByteBuffer;

public interface Reader<T> {
    enum ProcessStatus {
        DONE, REFILL, ERROR
    }
    ProcessStatus process(ByteBuffer buffer);
    T get();
    void reset();

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
