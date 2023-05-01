package fr.ramatellier.greed.server.model.component;

import java.nio.ByteBuffer;
import java.util.Objects;

public record CheckerComponent(String url, String className) implements GreedComponent {
    public CheckerComponent {
        Objects.requireNonNull(url);
        Objects.requireNonNull(className);
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        var urlPacket = new StringComponent(url);
        var classNamePacket = new StringComponent(className);
        urlPacket.putInBuffer(buffer);
        classNamePacket.putInBuffer(buffer);
    }

    @Override
    public int size() {
        return Integer.BYTES * 2 + url.length() + className.length();
    }
}
