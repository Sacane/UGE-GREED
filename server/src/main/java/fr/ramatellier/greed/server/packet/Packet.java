package fr.ramatellier.greed.server.packet;

import java.nio.ByteBuffer;

public interface Packet {
    void putInBuffer(ByteBuffer buffer);
    int size();
}
