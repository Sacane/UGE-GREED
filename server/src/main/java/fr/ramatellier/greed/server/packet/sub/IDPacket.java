package fr.ramatellier.greed.server.packet.sub;

import fr.ramatellier.greed.server.packet.Packet;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class IDPacket implements Packet {
    private final IpAddressPacket ipAddressPacket;
    private final int port;
    private final InetSocketAddress address;

    public IDPacket(InetSocketAddress address) {
        ipAddressPacket = new IpAddressPacket(address.getHostName());
        this.port = address.getPort();
        this.address = address;
    }

    public String getHostname() {
        return address.getHostName();
    }

    public int getPort() {
        return port;
    }

    public InetSocketAddress getSocket() {
        return address;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        ipAddressPacket.putInBuffer(buffer);
        buffer.putInt(port);
    }

    @Override
    public int size() {
        return ipAddressPacket.size() + Integer.BYTES;
    }

    @Override
    public String toString() {
        return "IDPacket{" +
                "port=" + port +
                ", address=" + address +
                '}';
    }
}