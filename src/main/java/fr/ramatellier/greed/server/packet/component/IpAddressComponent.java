package fr.ramatellier.greed.server.packet.component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Objects;

public final class IpAddressComponent implements GreedComponent {
    private final byte size;
    private final String address;
    private final byte[] ipAddress;

    public IpAddressComponent(String address) {
        this.address = Objects.requireNonNull(address);
        this.size = address.contains(".") ? (byte)0x04 : (byte)0x06;
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
