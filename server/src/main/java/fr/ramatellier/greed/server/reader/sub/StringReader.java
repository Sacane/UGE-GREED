package fr.ramatellier.greed.server.reader.sub;

import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.primitive.IntReader;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringReader implements Reader<String> {
    private enum State {
        DONE, WAITING_INT, WAITING_STRING, ERROR
    }
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private final IntReader intReader = new IntReader();
    private ByteBuffer stringBuffer;
    private State state = State.WAITING_INT;
    private String value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_INT) {
            var status = intReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                var size = intReader.get();

                if(size < 0 || size > 1024) {
                    return ProcessStatus.ERROR;
                }
                stringBuffer = ByteBuffer.allocate(size);
                state = State.WAITING_STRING;
            }
        }
        if(state == State.WAITING_STRING) {
            fillBuffer(buffer, stringBuffer);

            if(!stringBuffer.hasRemaining()) {
                state = State.DONE;
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