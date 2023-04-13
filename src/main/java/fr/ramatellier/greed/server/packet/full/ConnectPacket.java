package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public record ConnectPacket(IDPacket idPacket) implements LocalPacket {


    @Override
    public OpCodes opCode() {
        return OpCodes.CONNECT;
    }

    @Override
    public void put(ByteBuffer buffer) {
        idPacket.putInBuffer(buffer);
    }

    @Override
    public int size() {
        return Byte.BYTES * 2 + idPacket.size();
    }
}
