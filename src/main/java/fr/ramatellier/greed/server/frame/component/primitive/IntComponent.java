package fr.ramatellier.greed.server.frame.component.primitive;

public class IntComponent extends PrimitiveComponent<Integer>{
    public IntComponent(int value) {
        super(value, Integer.BYTES, (anInt, buffer) -> buffer.putInt(anInt));
    }
}
