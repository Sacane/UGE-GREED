package fr.ramatellier.greed.server.reader.component;

import fr.ramatellier.greed.server.frame.component.StringComponent;
import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.primitive.IntReader;
import fr.ramatellier.greed.server.util.Buffers;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringReader implements Reader<StringComponent> {
    private enum State {
        DONE, WAITING_INT, WAITING_STRING, ERROR
    }
    private State state = State.WAITING_INT;
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private final IntReader intReader = new IntReader();
    private ByteBuffer stringBuffer;
    private StringComponent value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_INT) {
            Buffers.runOnProcess(
                    buffer,
                    intReader,
                    size -> {
                        if(size < 0 || size > 1024) {
                            state = State.ERROR;
                        } else {
                            stringBuffer = ByteBuffer.allocate(size);
                            state = State.WAITING_STRING;
                        }
                    },
                    () -> state = State.ERROR
            );
            if(state == State.ERROR) {
                return ProcessStatus.ERROR;
            }
        }
        if(state == State.WAITING_STRING) {
            Buffers.fillBuffer(buffer, stringBuffer);

            if(!stringBuffer.hasRemaining()) {
                state = State.DONE;
                stringBuffer.flip();
                value = new StringComponent(UTF8.decode(stringBuffer).toString());
            }
        }

        if (state != State.DONE) {
            return ProcessStatus.REFILL;
        }

        return ProcessStatus.DONE;
    }

    @Override
    public StringComponent get() {
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