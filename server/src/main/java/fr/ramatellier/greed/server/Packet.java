package fr.ramatellier.greed.server;

import java.nio.ByteBuffer;

public interface Packet {
    void putInBuffer(ByteBuffer buffer);
}
