package fr.ramatellier.greed.server.reader.component;

import fr.ramatellier.greed.server.model.component.DestinationPacket;
import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.util.Buffers;

import java.nio.ByteBuffer;

public class DestinationComponentReader implements Reader<DestinationPacket> {
    private enum State {
        DONE, WAITING_SRC, WAITING_DST, ERROR
    }
    private State state = State.WAITING_SRC;
    private final IDComponentReader idSrcReader = new IDComponentReader();
    private final IDComponentReader idDstReader = new IDComponentReader();
    private DestinationPacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }
        if(state == State.WAITING_SRC) {
            Buffers.runOnProcess(buffer, idSrcReader,
                    __ -> state = State.WAITING_DST,
                    () -> {},
                    () -> state = State.ERROR);
            if(state == State.ERROR) {
                return ProcessStatus.ERROR;
            }
        }
        if(state == State.WAITING_DST) {
            Buffers.runOnProcess(buffer, idDstReader,
                    result -> {
                        state = State.DONE;
                        value = new DestinationPacket(idSrcReader.get().getSocket(), idDstReader.get().getSocket());
                    },
                    () -> state = State.ERROR);
            if(state == State.ERROR) {
                return ProcessStatus.ERROR;
            }
        }
        if (state != State.DONE) {
            return ProcessStatus.REFILL;
        }
        return ProcessStatus.DONE;
    }

    @Override
    public DestinationPacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }

        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_SRC;
        idSrcReader.reset();
        idDstReader.reset();
    }
}
