package fr.ramatellier.greed.server.frame.component.primitive;

import fr.ramatellier.greed.server.frame.component.GreedComponent;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.BiConsumer;

public abstract class PrimitiveComponent<T> implements GreedComponent {
    private final T value;
    private final int size;
    private final BiConsumer<T, ? super ByteBuffer> putMethod;
    public PrimitiveComponent(T value, int size, BiConsumer<T, ? super ByteBuffer> putMethod){
        this.value = Objects.requireNonNull(value);
        this.size = size;
        this.putMethod = Objects.requireNonNull(putMethod);
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        Objects.requireNonNull(buffer);
        putMethod.accept(value, buffer);
    }

    public T get(){
        return value;
    }

    @Override
    public int size() {
        return size;
    }

}
