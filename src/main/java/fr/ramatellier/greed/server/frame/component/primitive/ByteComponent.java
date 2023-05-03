package fr.ramatellier.greed.server.frame.component.primitive;

import fr.ramatellier.greed.server.reader.component.ByteComponentReader;

public class ByteComponent extends PrimitiveComponent<Byte> {
    public ByteComponent(Byte value) {
        super(value, Byte.BYTES, (aByte, buffer) -> buffer.put(aByte), new ByteComponentReader());
    }
}
