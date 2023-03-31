package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.DisconnectedPacket;

import java.nio.ByteBuffer;

public class DisconnectedPacketReader implements Reader<DisconnectedPacket> {
    private enum State {
        DONE, WAITING_IDSRC, WAITING_ID, ERROR
    }
    private State state = State.WAITING_IDSRC;
    private final IDReader idSrcReader = new IDReader();
    private final IDReader idReader = new IDReader();
    private DisconnectedPacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_IDSRC) {
            var status = idSrcReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_ID;
            }
        }
        if(state == State.WAITING_ID) {
            var status = idReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.DONE;

                value = new DisconnectedPacket(idSrcReader.get().getSocket(), idReader.get().getSocket());
            }
        }

        if (state != State.DONE) {
            return ProcessStatus.REFILL;
        }

        return Reader.ProcessStatus.DONE;
    }

    @Override
    public DisconnectedPacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }

        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_IDSRC;
        idSrcReader.reset();
        idReader.reset();
    }
}
