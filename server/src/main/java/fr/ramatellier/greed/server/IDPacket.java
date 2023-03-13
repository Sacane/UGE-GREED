package fr.ramatellier.greed.server;

import fr.ramatellier.greed.server.packet.Packet;
import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;

public class IDPacket implements Packet {
    private final IPPacket ipPacket;
    private final int port;

    public IDPacket(String address, int port) {
        ipPacket = new IPPacket(address);
        this.port = port;
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
