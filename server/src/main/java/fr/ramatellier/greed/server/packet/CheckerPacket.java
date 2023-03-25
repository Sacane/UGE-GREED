package fr.ramatellier.greed.server.packet;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class CheckerPacket implements Packet {
    private final String url;
    private final String className;

    public CheckerPacket(String url, String className) {
        Objects.requireNonNull(url);
        Objects.requireNonNull(className);
        this.url = url;
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        var urlPacket = new StringPacket(url);
        var classNamePacket = new StringPacket(className);
        urlPacket.putInBuffer(buffer);
        classNamePacket.putInBuffer(buffer);
    }
}
