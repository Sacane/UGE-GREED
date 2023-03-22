package fr.ramatellier.greed.server.packet;

import java.nio.ByteBuffer;

public class RangePacket implements Packet {
    private final long start;
    private final long end;

    public RangePacket(long start, long end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        buffer.putLong(start);
        buffer.putLong(end);
    }
}
