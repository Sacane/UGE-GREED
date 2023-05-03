package fr.ramatellier.greed.server.reader.component;

import fr.ramatellier.greed.server.frame.component.primitive.ByteComponent;

public class ByteComponentReader extends PrimitiveComponentReader<ByteComponent>{
    public ByteComponentReader() {
        super(Byte.BYTES, buffer -> new ByteComponent(buffer.get()));
    }
}
