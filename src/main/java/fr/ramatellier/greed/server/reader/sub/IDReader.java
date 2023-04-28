package fr.ramatellier.greed.server.reader.sub;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.primitive.IntReader;
import fr.ramatellier.greed.server.util.Buffers;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class IDReader implements Reader<IDPacket> {
    private enum State {
        DONE, WAITING_IP, WAITING_PORT, ERROR
    }
    private State state = State.WAITING_IP;
    private final IPReader ipReader = new IPReader();
    private final IntReader portReader = new IntReader();
    private IDPacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_IP) {
            Buffers.runOnProcess(buffer, ipReader,
                    __ -> state = State.WAITING_PORT,
                    () -> {},
                    () -> state = State.ERROR);
        }
        if(state == State.WAITING_PORT) {
            Buffers.runOnProcess(buffer, portReader,
                    result -> {
                        state = State.DONE;
                        value = new IDPacket(new InetSocketAddress(ipReader.get().getAddress(), result));
                    },
                    () -> {},
                    () -> state = State.ERROR);
        }
        if (state != State.DONE) {
            return ProcessStatus.REFILL;
        }

        return ProcessStatus.DONE;
    }

    @Override
    public IDPacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }

        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_IP;
        ipReader.reset();
        portReader.reset();
    }
}
