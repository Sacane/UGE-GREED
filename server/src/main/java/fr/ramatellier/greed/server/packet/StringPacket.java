package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.Server;

import java.nio.ByteBuffer;


public record StringPacket(String value) implements Packet{
    @Override
    public void putInBuffer(ByteBuffer buffer) {
        buffer.putInt(value.length()).put(Server.UTF8.encode(value));
    }
}
