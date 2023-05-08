package fr.ramatellier.greed.server.frame.component;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public record StringComponent(String value) implements GreedComponent {
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        buffer.putInt(value.length()).put(UTF8.encode(value));
    }

    @Override
    public int size() {
        return Integer.BYTES + value.length();
    }

}
