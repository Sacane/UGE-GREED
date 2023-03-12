package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;

public interface Packet {
    TramKind kind();
    byte opCode();
    void putInBuffer(ByteBuffer buffer);
}
