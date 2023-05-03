package fr.ramatellier.greed.server.frame.component;

import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.component.DestinationComponentReader;

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

    @Override
    public Reader<? extends GreedComponent> reader() {
        return new DestinationComponentReader();
    }
}
