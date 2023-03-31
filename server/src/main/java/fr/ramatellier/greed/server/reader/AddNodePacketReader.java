package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.AddNodePacket;

import java.nio.ByteBuffer;

public class AddNodePacketReader implements Reader<AddNodePacket> {
    private enum State {
        DONE, WAITING_ID, WAITING_DAUGHTER, ERROR
    }
    private State state = State.WAITING_ID;
    private final IDReader idReader = new IDReader();
    private final IDReader idDaughterReader = new IDReader();
    private AddNodePacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_ID) {
            var status = idReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_DAUGHTER;
            }
        }
        if(state == State.WAITING_DAUGHTER) {
            var status = idDaughterReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.DONE;

                value = new AddNodePacket(idReader.get(), idDaughterReader.get());
            }
        }

        if (state != State.DONE) {
            return ProcessStatus.REFILL;
        }

        return Reader.ProcessStatus.DONE;
    }

    @Override
    public AddNodePacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }

        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_ID;
        idReader.reset();
        idDaughterReader.reset();
    }
}
