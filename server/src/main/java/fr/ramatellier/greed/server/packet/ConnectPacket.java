package fr.ramatellier.greed.server.packet;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class ConnectPacket implements Packet {
    private final IDPacket idPacket;

    public ConnectPacket(InetSocketAddress address) {
        idPacket = new IDPacket(address);
    }

    public String getAddress() {
        return idPacket.getAddress();
    }

    public int getPort() {
        return idPacket.getPort();
    }

    public InetSocketAddress getSocket() {
        return idPacket.getSocket();
    }

    public void putInBuffer(ByteBuffer buffer) {
        buffer.put((byte) 0);
        buffer.put((byte) 1);
        idPacket.putInBuffer(buffer);
    }
}
