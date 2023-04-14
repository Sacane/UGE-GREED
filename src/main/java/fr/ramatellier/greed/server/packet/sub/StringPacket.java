package fr.ramatellier.greed.server.packet.sub;

import fr.ramatellier.greed.server.packet.Packet;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public record StringPacket(String value) implements Packet, Part {
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