package fr.ramatellier.greed.server.packet.sub;

import fr.ramatellier.greed.server.packet.Packet;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class IpAddressPacket implements Packet {
    private final byte size;
    private final String address;
    private final byte[] ipAddress;

    public IpAddressPacket(String address) {
        this.size = address.contains(".") ? (byte)0x04 : (byte)0x06;
        this.address = address;
        try {
            ipAddress = InetAddress.getByName(address).getAddress();
        } catch (UnknownHostException e) {
            throw new AssertionError(e);
        }
    }

    public String getAddress() {
        return address;
    }

    public void putInBuffer(ByteBuffer buffer) {
        buffer.put(size);
        buffer.put(ipAddress);

    }

    @Override
    public int size() {
        return Byte.BYTES + ipAddress.length;
    }
}
