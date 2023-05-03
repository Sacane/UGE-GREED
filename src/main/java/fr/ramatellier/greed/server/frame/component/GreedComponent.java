package fr.ramatellier.greed.server.frame.component;

import fr.ramatellier.greed.server.reader.Reader;

import java.nio.ByteBuffer;

/**
 * A GreedComponent is a part of a frame that respect the Greed protocol.
 * A GreedComponent has a size corresponding to the byte size of its content, and can be put it in a buffer.
 */
public interface GreedComponent {
    void putInBuffer(ByteBuffer buffer);
    int size();
    Reader<? extends GreedComponent> reader();
}
