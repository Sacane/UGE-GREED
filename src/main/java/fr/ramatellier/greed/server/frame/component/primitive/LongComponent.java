package fr.ramatellier.greed.server.frame.component.primitive;

public class LongComponent extends PrimitiveComponent<Long> {
    private LongComponent(long value) {
        super(value, Long.BYTES, (aLong, buffer) -> buffer.putLong(aLong));
    }
    public static LongComponent of(Long value){
        return new LongComponent(value);
    }
}
