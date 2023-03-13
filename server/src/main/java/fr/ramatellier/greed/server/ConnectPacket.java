package fr.ramatellier.greed.server;

import fr.ramatellier.greed.server.packet.Packet;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class ConnectPacket implements Packet {
    private final IDPacket idPacket;

    public ConnectPacket(InetSocketAddress address) {
        idPacket = new IDPacket(address.getHostName(), address.getPort());
    }


    public void putInBuffer(ByteBuffer buffer) {
        buffer.put((byte) 0);
        buffer.put((byte) 1);
        idPacket.putInBuffer(buffer);
    }
}
