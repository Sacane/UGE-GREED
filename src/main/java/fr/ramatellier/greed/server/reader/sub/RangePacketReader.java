package fr.ramatellier.greed.server.reader.sub;

import fr.ramatellier.greed.server.packet.sub.RangePacket;
import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.primitive.LongReader;

import java.nio.ByteBuffer;

public class RangePacketReader implements Reader<RangePacket> {
    private enum State {
        DONE, WAITING_START, WAITING_END, ERROR
    }
    private State state = State.WAITING_START;
    private final LongReader startReader = new LongReader();
    private final LongReader endReader = new LongReader();
    private RangePacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_START) {
            var status = startReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_END;
            }
        }
        if(state == State.WAITING_END) {
            var status = endReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.DONE;

                value = new RangePacket(startReader.get(), endReader.get());
            }
        }

        if (state != State.DONE) {
            return ProcessStatus.REFILL;
        }

        return Reader.ProcessStatus.DONE;
    }

    @Override
    public RangePacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }

        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_START;
        startReader.reset();
        endReader.reset();
    }
}
