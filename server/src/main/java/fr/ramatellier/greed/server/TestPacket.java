package fr.ramatellier.greed.server;

import java.nio.ByteBuffer;

public class TestPacket implements Packet {
    private final String msg;

    public TestPacket(String msg) {
        this.msg = msg;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
    }

    @Override
    public String toString() {
        return msg;
    }
}
