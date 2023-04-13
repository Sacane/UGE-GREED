package fr.ramatellier.greed.server.reader.full;

import fr.ramatellier.greed.server.packet.full.PleaseReconnectPacket;
import fr.ramatellier.greed.server.reader.FullPacketReader;
import fr.ramatellier.greed.server.reader.sub.IDReader;
import fr.ramatellier.greed.server.reader.Reader;

import java.nio.ByteBuffer;

public class PleaseReconnectPacketReader implements FullPacketReader {
    private enum State {
        DONE, WAITING_ID, ERROR
    }
    private State state = State.WAITING_ID;
    private final IDReader idReader = new IDReader();
    private PleaseReconnectPacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_ID) {
            var status = idReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.DONE;

                value = new PleaseReconnectPacket(idReader.get());
            }
        }

        if (state != State.DONE) {
            return ProcessStatus.REFILL;
        }

        return Reader.ProcessStatus.DONE;
    }

    @Override
    public PleaseReconnectPacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }

        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_ID;
        idReader.reset();
    }
}
