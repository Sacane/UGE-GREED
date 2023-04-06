package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;

public interface Packet {
    void putInBuffer(ByteBuffer buffer);
    int size();
}
