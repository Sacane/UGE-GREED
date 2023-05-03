package fr.ramatellier.greed.server.frame.component.primitive;

import fr.ramatellier.greed.server.reader.component.LongComponentReader;

public class LongComponent extends PrimitiveComponent<Long> {
    public LongComponent(Long value) {
        super(value, Long.BYTES, (aLong, buffer) -> buffer.putLong(aLong), new LongComponentReader());
    }

}
