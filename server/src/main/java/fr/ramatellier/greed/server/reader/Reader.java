package fr.ramatellier.greed.server.reader;

import java.nio.ByteBuffer;

public interface Reader<T> {
    public static enum ProcessStatus { DONE, REFILL, ERROR };

    public ProcessStatus process(ByteBuffer bb);

    public T get();

    public void reset();

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
