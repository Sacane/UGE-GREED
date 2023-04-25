package fr.ramatellier.greed.server.util;

import fr.ramatellier.greed.server.reader.Reader;

import java.nio.ByteBuffer;

/**
 * Singletons use to fill buffer and list
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

//    public static <E extends Packet> void fillList(ArrayList<E> list, int size, Reader<E> reader, ByteBuffer buffer) {
//        while(buffer.limit() > 0 && list.size() != size) {
//            var status = reader.process(buffer);
//
//            if(status == Reader.ProcessStatus.DONE) {
//                list.add(reader.get());
//                reader.reset();
//            }
//        }
//    }

    /**
     * Run the reader process and call the corresponding function.
     * @param buffer the buffer to process
     * @param reader the reader to use to read the buffer
     * @param onDone the function to call if the reader is done
     * @param onRefill the function to call if the reader need more data
     * @param onError the function to call if the reader encounter an error
     */
    public static void runOnProcess(ByteBuffer buffer, Reader<?> reader, Runnable onDone, Runnable onRefill, Runnable onError){
        switch(reader.process(buffer)){
            case DONE -> onDone.run();
            case REFILL -> onRefill.run();
            case ERROR -> onError.run();
        }
    }
}
