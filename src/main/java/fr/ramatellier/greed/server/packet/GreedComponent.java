package fr.ramatellier.greed.server.packet;

import java.nio.ByteBuffer;

public interface GreedComponent {
    void putInBuffer(ByteBuffer buffer);
    int size();
}
