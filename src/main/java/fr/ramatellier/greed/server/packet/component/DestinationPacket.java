package fr.ramatellier.greed.server.packet.component;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public final class DestinationPacket implements GreedComponent {
    private final IDComponent idSrc;
    private final IDComponent idDst;

    public DestinationPacket(InetSocketAddress srcAddress, InetSocketAddress dstAddress) {
        idSrc = new IDComponent(srcAddress);
        idDst = new IDComponent(dstAddress);
    }

    public IDComponent getIdSrc() {
        return idSrc;
    }

    public IDComponent getIdDst() {
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
