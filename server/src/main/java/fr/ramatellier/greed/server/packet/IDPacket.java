package fr.ramatellier.greed.server.packet;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class IDPacket implements Packet {
    private final IpAddress ipAddress;
    private final int port;
    private final InetSocketAddress address;

    public IDPacket(InetSocketAddress address) {
        ipAddress = new IpAddress(address.getHostName());
        this.port = address.getPort();
        this.address = address;
    }

    public String getAddress() {
        return ipAddress.getAddress();
    }

    public int getPort() {
        return port;
    }

    public InetSocketAddress getSocket() {
        return address;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        ipAddress.putInBuffer(buffer);
        buffer.putInt(port);
    }
}
