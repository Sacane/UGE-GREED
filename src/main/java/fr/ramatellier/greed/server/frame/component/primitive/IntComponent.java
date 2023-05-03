package fr.ramatellier.greed.server.frame.component.primitive;

import fr.ramatellier.greed.server.reader.component.IntComponentReader;

public class IntComponent extends PrimitiveComponent<Integer>{
    public IntComponent(int value) {
        super(value, Integer.BYTES, (anInt, buffer) -> buffer.putInt(anInt), new IntComponentReader());
    }
}
