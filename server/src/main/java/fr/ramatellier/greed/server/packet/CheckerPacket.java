package fr.ramatellier.greed.server.packet;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class CheckerPacket implements Packet {
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private final String url;
    private final String className;

    public CheckerPacket(String url, String className){
        this.url = Objects.requireNonNull(url);
        this.className = Objects.requireNonNull(className);
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        var encodeURL = UTF8.encode(url);
        var encodeClassName = UTF8.encode(className);

        buffer.putInt(encodeURL.remaining());
        buffer.put(encodeURL);
        buffer.putInt(encodeClassName.remaining());
        buffer.put(encodeClassName);
    }
}
