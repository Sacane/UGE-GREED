package fr.ramatellier.greed.server.frame.component;

import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.component.DestinationComponentReader;

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

    @Override
    public Reader<? extends GreedComponent> reader() {
        return new DestinationComponentReader();
    }
}
