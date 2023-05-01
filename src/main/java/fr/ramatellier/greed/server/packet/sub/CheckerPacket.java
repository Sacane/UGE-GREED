package fr.ramatellier.greed.server.packet.sub;

import fr.ramatellier.greed.server.packet.GreedComponent;

import java.nio.ByteBuffer;
import java.util.Objects;

public record CheckerPacket(String url, String className) implements GreedComponent {
    public CheckerPacket {
        Objects.requireNonNull(url);
        Objects.requireNonNull(className);
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        var urlPacket = new StringPacket(url);
        var classNamePacket = new StringPacket(className);
        urlPacket.putInBuffer(buffer);
        classNamePacket.putInBuffer(buffer);
    }

    @Override
    public int size() {
        return Integer.BYTES * 2 + url.length() + className.length();
    }
}
