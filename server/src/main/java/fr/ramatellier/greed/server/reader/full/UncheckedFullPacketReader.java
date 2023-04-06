package fr.ramatellier.greed.server.reader.full;

import fr.ramatellier.greed.server.packet.full.FullPacket;
import fr.ramatellier.greed.server.reader.FullPacketReader;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

public abstract class UncheckedFullPacketReader<T extends FullPacket> implements FullPacketReader {
    private final T packet;

    public UncheckedFullPacketReader(Supplier<T> supplier) {
        this.packet = supplier.get();
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
