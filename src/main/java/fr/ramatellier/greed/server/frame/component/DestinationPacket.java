package fr.ramatellier.greed.server.frame.component;

import java.nio.ByteBuffer;

public record DestinationPacket(StringComponent idSrc, StringComponent idDst) implements GreedComponent {

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
