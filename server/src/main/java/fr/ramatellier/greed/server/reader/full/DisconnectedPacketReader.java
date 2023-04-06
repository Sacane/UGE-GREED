package fr.ramatellier.greed.server.reader.full;

import fr.ramatellier.greed.server.packet.full.DisconnectedPacket;
import fr.ramatellier.greed.server.reader.FullPacketReader;
import fr.ramatellier.greed.server.reader.sub.IDReader;
import fr.ramatellier.greed.server.reader.Reader;

import java.nio.ByteBuffer;

public class DisconnectedPacketReader implements FullPacketReader {
    private enum State {
        DONE, WAITING_ID_SRC, WAITING_ID, ERROR
    }
    private State state = State.WAITING_ID_SRC;
    private final IDReader idSrcReader = new IDReader();
    private final IDReader idReader = new IDReader();
    private DisconnectedPacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_ID_SRC) {
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
        state = State.WAITING_ID_SRC;
        idSrcReader.reset();
        idReader.reset();
    }
}
