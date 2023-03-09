package fr.ramatellier.greed.server.reader;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static fr.ramatellier.greed.server.reader.Reader.ProcessStatus.DONE;
import static fr.ramatellier.greed.server.reader.Reader.ProcessStatus.REFILL;

public class StringReader implements Reader<String> {
    private enum State {
        DONE, WAITING_INT, WAITING_STRING, ERROR
    };
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private final IntReader intReader = new IntReader();
    private int stringBufferSize;
    private ByteBuffer stringBuffer;
    private State state = State.WAITING_INT;
    private String value;

    private static void fillBuffer(ByteBuffer srcBuffer, ByteBuffer dstBuffer) {
        if (srcBuffer.remaining() <= dstBuffer.remaining()) {
            dstBuffer.put(srcBuffer);
        } else {
            var oldLimit = srcBuffer.limit();
            srcBuffer.limit(srcBuffer.position() + dstBuffer.remaining());
            dstBuffer.put(srcBuffer);
            srcBuffer.limit(oldLimit);
        }
    }

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_INT) {
            var status = intReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                stringBufferSize = intReader.get();

                if(stringBufferSize < 0 || stringBufferSize > 1024) {
                    return ProcessStatus.ERROR;
                }
                stringBuffer = ByteBuffer.allocate(stringBufferSize);
                state = State.WAITING_STRING;
            }
        }
        if(state == State.WAITING_STRING) {
            buffer.flip();
            try {
                fillBuffer(buffer, stringBuffer);

                if(!stringBuffer.hasRemaining()) {
                    state = State.DONE;
                }
            } finally {
                buffer.compact();
            }
        }

        if (state == State.WAITING_INT || state == State.WAITING_STRING) {
            return ProcessStatus.REFILL;
        }

        stringBuffer.flip();
        value = UTF8.decode(stringBuffer).toString();

        return ProcessStatus.DONE;
    }

    @Override
    public String get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }

        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_INT;
        intReader.reset();
    }
}