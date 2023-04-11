package fr.ramatellier.greed.server.reader.sub;

import fr.ramatellier.greed.server.packet.sub.DestinationPacket;
import fr.ramatellier.greed.server.reader.Reader;

import java.nio.ByteBuffer;

public class DestinationPacketReader implements Reader<DestinationPacket> {
    private enum State {
        DONE, WAITING_SRC, WAITING_DST, ERROR
    }
    private State state = State.WAITING_SRC;
    private final IDReader idSrcReader = new IDReader();
    private final IDReader idDstReader = new IDReader();
    private DestinationPacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_SRC) {
            var status = idSrcReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_DST;
            }
        }
        if(state == State.WAITING_DST) {
            var status = idDstReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.DONE;

                value = new DestinationPacket(idSrcReader.get().getSocket(), idDstReader.get().getSocket());
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
