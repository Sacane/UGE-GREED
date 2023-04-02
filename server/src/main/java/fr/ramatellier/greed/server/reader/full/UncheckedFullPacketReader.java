package fr.ramatellier.greed.server.reader.full;

import fr.ramatellier.greed.server.packet.full.FullPacket;
import fr.ramatellier.greed.server.reader.FullPacketReader;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

public abstract class UncheckedFullPacketReader<T extends FullPacket> implements FullPacketReader {

    private final T packet;
    public UncheckedFullPacketReader(Class<T> clazz) {
        try {
            this.packet = clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new AssertionError();
        }
    }

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        return ProcessStatus.DONE;
    }

    public T get() {
        return packet;
    }

    @Override
    public void reset() {

    }
}
