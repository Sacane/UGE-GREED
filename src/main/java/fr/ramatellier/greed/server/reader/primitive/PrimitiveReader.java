package fr.ramatellier.greed.server.reader.primitive;

import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.Buffers;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * Perform reader of a primitive Value, passing its number of bytes in constructor.
 * @param <T>
 */
public abstract class PrimitiveReader<T> implements Reader<T> {
    private enum State {
        DONE, WAITING, ERROR
    }
    private State state = State.WAITING;
    private final ByteBuffer internalBuffer; // write-mode
    private T value;
    private final Function<ByteBuffer, T> converter;

    public PrimitiveReader(int nbByte, Function<ByteBuffer, T> converter) {
        this.internalBuffer = ByteBuffer.allocate(nbByte);
        this.converter = converter;
    }

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        Buffers.fillBuffer(buffer, internalBuffer);

        if (internalBuffer.hasRemaining()) {
            return Reader.ProcessStatus.REFILL;
        }

        state = State.DONE;
        internalBuffer.flip();
        value = converter.apply(internalBuffer);

        return Reader.ProcessStatus.DONE;
    }

    @Override
    public T get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING;
        internalBuffer.clear();
    }
}
