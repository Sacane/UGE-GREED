package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.util.TramKind;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class IPPacket implements Packet {
    private final byte size;
    private final String address;

    public IPPacket(String address) {
        this.size = address.contains(".") ? (byte)0x04 : (byte)0x06;
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public TramKind kind() {
        return TramKind.ANY;
    }

    @Override
    public byte opCode() {
        return 0;
    }

    public void putInBuffer(ByteBuffer buffer) {
        buffer.put(size);
        try {
            var ipAddress = InetAddress.getByName(address).getAddress();
            buffer.put(ipAddress);
        } catch (UnknownHostException e) {
            throw new AssertionError(e);
        }

    }
}