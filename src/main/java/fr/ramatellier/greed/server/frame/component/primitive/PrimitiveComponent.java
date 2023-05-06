package fr.ramatellier.greed.server.frame.component.primitive;

import fr.ramatellier.greed.server.frame.component.GreedComponent;
import fr.ramatellier.greed.server.reader.Reader;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.BiConsumer;

public abstract class PrimitiveComponent<T> implements GreedComponent {
    private final T value;
    private final int size;
    private final Reader<? extends GreedComponent> type;
    private final BiConsumer<T, ? super ByteBuffer> putMethod;
    public PrimitiveComponent(T value, int size, BiConsumer<T, ByteBuffer> putMethod, Reader<? extends GreedComponent> type){
        this.value = Objects.requireNonNull(value);
        this.size = size;
        this.putMethod = Objects.requireNonNull(putMethod);
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        putMethod.accept(value, buffer);
    }

    public T get(){
        return value;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Reader<? extends GreedComponent> reader() {
        return type;
    }
}
