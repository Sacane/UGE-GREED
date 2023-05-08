package fr.ramatellier.greed.server.frame.component.primitive;

public class ByteComponent extends PrimitiveComponent<Byte> {
    public ByteComponent(byte value) {
        super(value, Byte.BYTES, (aByte, buffer) -> buffer.put(aByte));
    }
}
