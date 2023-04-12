package fr.ramatellier.greed.server.util;

import fr.ramatellier.greed.server.packet.Packet;
import fr.ramatellier.greed.server.reader.Reader;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Singletons use to fill buffer and list
 */
public final class Buffers {
    private Buffers() {}
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

    public static <E extends Packet> void fillList(ArrayList<E> list, int size, Reader<E> reader, ByteBuffer buffer) {
        while(buffer.limit() > 0 && list.size() != size) {
            var status = reader.process(buffer);

            if(status == Reader.ProcessStatus.DONE) {
                list.add(reader.get());
                reader.reset();
            }
        }
    }
}
