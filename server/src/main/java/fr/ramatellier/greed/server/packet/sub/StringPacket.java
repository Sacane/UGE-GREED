package fr.ramatellier.greed.server.packet.sub;

import fr.ramatellier.greed.server.Server;
import fr.ramatellier.greed.server.packet.Packet;

import java.nio.ByteBuffer;


public record StringPacket(String value) implements Packet {
    @Override
    public void putInBuffer(ByteBuffer buffer) {
        buffer.putInt(value.length()).put(Server.UTF8.encode(value));
    }
}
