package fr.ramatellier.greed.server.reader;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Singletons use to perform action through buffers and readers.
 */
public final class Buffers {
    private Buffers() {}

    /**
     * Fill the dstBuffer with the srcBuffer
     * @param srcBuffer the source buffer
     * @param dstBuffer the destination buffer
     */
    public static void fillBuffer(ByteBuffer srcBuffer, ByteBuffer dstBuffer) {
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

    /**
     * Run the reader process and call the corresponding function.
     * @param buffer the buffer to process
     * @param reader the reader to use to read the buffer
     * @param onDone the function to call if the reader is done
     * @param onRefill the function to call if the reader need more data
     * @param onError the function to call if the reader encounter an error
     * @param <T> the type of the object the reader will return
     */
    public static <T> void runOnProcess(ByteBuffer buffer, Reader<T> reader, Consumer<T> onDone, Runnable onRefill, Runnable onError){
        Objects.requireNonNull(buffer);
        Objects.requireNonNull(reader);
        Objects.requireNonNull(onDone);
        Objects.requireNonNull(onRefill);
        Objects.requireNonNull(onError);
        switch(reader.process(buffer)){
            case DONE -> onDone.accept(reader.get());
            case REFILL -> onRefill.run();
            case ERROR -> onError.run();
        }
    }

    /**
     * Run the reader process and call the corresponding function, except the onRefill case.
     * @param buffer the buffer to process
     * @param reader the reader to use to read the buffer
     * @param onDone the function to call if the reader is done
     * @param onError the function to call if the reader encounter an error
     * @param <T> the type of the object the reader will return
     */
    public static <T> void runOnProcess(ByteBuffer buffer, Reader<T> reader, Consumer<T> onDone, Runnable onError){
        runOnProcess(buffer, reader, onDone, () -> {}, onError);
    }
}
