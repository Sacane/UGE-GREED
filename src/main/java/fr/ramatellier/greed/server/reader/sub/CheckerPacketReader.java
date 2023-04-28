package fr.ramatellier.greed.server.reader.sub;

import fr.ramatellier.greed.server.packet.sub.CheckerPacket;
import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.util.Buffers;

import java.nio.ByteBuffer;

public class CheckerPacketReader implements Reader<CheckerPacket> {
    private enum State {
        DONE, WAITING_URL, WAITING_CLASSNAME, ERROR
    }
    private State state = State.WAITING_URL;
    private final StringReader urlReader = new StringReader();
    private final StringReader classNameReader = new StringReader();
    private CheckerPacket value;

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
                        value = new CheckerPacket(urlReader.get(), result);
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
    public CheckerPacket get() {
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
