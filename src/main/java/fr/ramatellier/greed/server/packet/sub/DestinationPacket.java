package fr.ramatellier.greed.server.packet.sub;

import fr.ramatellier.greed.server.packet.Packet;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public final class DestinationPacket implements Packet, Part {
    private final IDPacket idSrc;
    private final IDPacket idDst;

    public DestinationPacket(InetSocketAddress srcAddress, InetSocketAddress dstAddress) {
        idSrc = new IDPacket(srcAddress);
        idDst = new IDPacket(dstAddress);
    }

    public IDPacket getIdSrc() {
        return idSrc;
    }

    public IDPacket getIdDst() {
        return idDst;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        idSrc.putInBuffer(buffer);
        idDst.putInBuffer(buffer);
    }

    @Override
    public int size() {
        return idSrc.size() + idDst.size();
    }
}
