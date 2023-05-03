package fr.ramatellier.greed.server.reader.component;

import fr.ramatellier.greed.server.frame.component.primitive.LongComponent;

public class LongComponentReader extends PrimitiveComponentReader<LongComponent> {
    public LongComponentReader() {
        super(Long.BYTES, buffer -> LongComponent.of(buffer.getLong()));
    }
}
