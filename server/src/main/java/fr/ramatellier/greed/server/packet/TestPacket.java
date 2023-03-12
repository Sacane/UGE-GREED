package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.packet.Packet;
import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;

public class TestPacket implements Packet {
    private final String msg;

    public TestPacket(String msg) {
        this.msg = msg;
    }

    @Override
    public TramKind kind() {
        return null;
    }

    @Override
    public byte opCode() {
        return 0;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
    }

    @Override
    public String toString() {
        return msg;
    }
}
