package fr.ramatellier.greed.server.reader.component.primitive;

import fr.ramatellier.greed.server.frame.component.primitive.IntComponent;

public class IntComponentReader extends PrimitiveComponentReader<IntComponent>{
    public IntComponentReader() {
        super(Integer.BYTES, buffer -> new IntComponent(buffer.getInt()));
    }
}