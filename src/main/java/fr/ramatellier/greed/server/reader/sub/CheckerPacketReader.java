package fr.ramatellier.greed.server.reader.sub;

import fr.ramatellier.greed.server.packet.component.CheckerComponent;
import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.util.Buffers;

import java.nio.ByteBuffer;

public class CheckerPacketReader implements Reader<CheckerComponent> {
    private enum State {
        DONE, WAITING_URL, WAITING_CLASSNAME, ERROR
    }
    private State state = State.WAITING_URL;
    private final StringReader urlReader = new StringReader();
    private final StringReader classNameReader = new StringReader();
    private CheckerComponent value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }
        if(state == State.WAITING_URL) {
            Buffers.runOnProcess(buffer, urlReader,
                    __ -> state = State.WAITING_CLASSNAME,
                    () -> {},
                    () -> state = State.ERROR);
        }
        if(state == State.WAITING_CLASSNAME) {
            Buffers.runOnProcess(buffer, classNameReader,
                    result -> {
                        state = State.DONE;
                        value = new CheckerComponent(urlReader.get(), result);
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
    public CheckerComponent get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_URL;
        urlReader.reset();
        classNameReader.reset();
    }
}
