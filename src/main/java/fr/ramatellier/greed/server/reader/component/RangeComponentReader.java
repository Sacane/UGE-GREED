package fr.ramatellier.greed.server.reader.component;

import fr.ramatellier.greed.server.frame.component.RangeComponent;
import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.primitive.LongReader;
import fr.ramatellier.greed.server.util.Buffers;

import java.nio.ByteBuffer;

public class RangeComponentReader implements Reader<RangeComponent> {
    private enum State {
        DONE, WAITING_START, WAITING_END, ERROR
    }
    private State state = State.WAITING_START;
    private final LongReader startReader = new LongReader();
    private final LongReader endReader = new LongReader();
    private RangeComponent value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_START) {
            Buffers.runOnProcess(buffer, startReader,
                    __ -> state = State.WAITING_END,
                    () -> state = State.ERROR);
        }
        if(state == State.WAITING_END) {
            Buffers.runOnProcess(buffer, endReader,
                    result -> {
                        state = State.DONE;
                        value = new RangeComponent(startReader.get(), result);
                    },
                    () -> {},
                    () -> state = State.ERROR);
        }

        if (state != State.DONE) {
            return ProcessStatus.REFILL;
        }

        return Reader.ProcessStatus.DONE;
    }

    @Override
    public RangeComponent get() {
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
