package fr.ramatellier.greed.server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class IDPacket implements Packet {
    private final IPPacket ipPacket;
    private final int port;
    private final InetSocketAddress address;

    public IDPacket(InetSocketAddress address) {
        ipPacket = new IPPacket(address.getHostName());
        this.port = address.getPort();
        this.address = address;
    }

    public String getAddress() {
        return ipPacket.getAddress();
    }

    public int getPort() {
        return port;
    }

    public InetSocketAddress getSocket() {
        return address;
    }

    public String getAddress() {
        return ipPacket.getAddress();
    }

    public int getPort() {
        return port;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        ipPacket.putInBuffer(buffer);
        buffer.putInt(port);
    }
}
