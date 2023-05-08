package fr.ramatellier.greed.server.reader.component.primitive;

import fr.ramatellier.greed.server.frame.component.GreedComponent;
import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.Buffers;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Function;

public class PrimitiveComponentReader<T extends GreedComponent> implements Reader<T> {
    private enum State {
        DONE, WAITING, ERROR
    }
    private State state = State.WAITING;
    private final ByteBuffer internalBuffer;
    private final Function<ByteBuffer, T> componentFactory;
    private T value;
    public PrimitiveComponentReader(int nbByte, Function<ByteBuffer, T> componentFactory) {
        Objects.requireNonNull(componentFactory);
        this.componentFactory = componentFactory;
        this.internalBuffer = ByteBuffer.allocate(nbByte);
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
        value = componentFactory.apply(internalBuffer);
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
