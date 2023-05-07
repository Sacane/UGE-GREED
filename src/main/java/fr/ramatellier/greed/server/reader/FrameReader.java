package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.frame.FrameKind;
import fr.ramatellier.greed.server.frame.OpCode;
import fr.ramatellier.greed.server.frame.model.Frame;
import fr.ramatellier.greed.server.reader.component.primitive.ByteComponentReader;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.Objects;

public class FrameReader implements Reader<Frame> {
    private enum State {
        DONE, WAITING_LOCATION, WAITING_CODE, WAITING_PACKET, ERROR
    }
    private final FrameReaderDecoder frameDecoder = new FrameReaderDecoder();
    private State state = State.WAITING_LOCATION;
    private final ByteComponentReader tramKindReader = new ByteComponentReader();
    private final ByteComponentReader opCodeReader = new ByteComponentReader();

    private Frame value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        Objects.requireNonNull(buffer);
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_LOCATION) {
            Buffers.runOnProcess(buffer,
                    tramKindReader,
                    __ -> state = State.WAITING_CODE,
                    () -> state = State.ERROR);
        }
        if(state == State.WAITING_CODE) {
            Buffers.runOnProcess(buffer,
                    opCodeReader,
                    __ -> state = State.WAITING_PACKET,
                    () -> state = State.ERROR);
        }
        if(state == State.WAITING_PACKET) {
            var tramKind = FrameKind.of(tramKindReader.get().get());
            if (tramKind == null) return ProcessStatus.ERROR;
            var opcode = OpCode.of(opCodeReader.get().get());
            if (opcode == null) return ProcessStatus.ERROR;
            try {
                var status = frameDecoder.process(buffer, opcode);
                if(status == ProcessStatus.DONE) {
                    state = State.DONE;
                    value = frameDecoder.get();
                }
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                return ProcessStatus.ERROR;
            }
        }
        if (state != State.DONE) {
            return ProcessStatus.REFILL;
        }
        return ProcessStatus.DONE;
    }

    @Override
    public Frame get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_LOCATION;
        tramKindReader.reset();
        opCodeReader.reset();
        frameDecoder.reset();
    }
}
