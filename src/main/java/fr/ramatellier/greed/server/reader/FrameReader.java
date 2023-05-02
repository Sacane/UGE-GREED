package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.frame.model.Frame;
import fr.ramatellier.greed.server.reader.primitive.ByteReader;
import fr.ramatellier.greed.server.util.Buffers;
import fr.ramatellier.greed.server.util.OpCode;
import fr.ramatellier.greed.server.frame.FrameKind;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

public class FrameReader implements FullPacketReader {
    private enum State {
        DONE, WAITING_LOCATION, WAITING_CODE, WAITING_PACKET, ERROR
    }
    private final FrameReaderAdapter fullReaderFactory = new FrameReaderAdapter();
    private State state = State.WAITING_LOCATION;
    private final ByteReader locationReader = new ByteReader();
    private final ByteReader codeReader = new ByteReader();

    private Frame value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_LOCATION) {
            Buffers.runOnProcess(buffer,
                    locationReader,
                    __ -> state = State.WAITING_CODE,
                    () -> state = State.ERROR);
        }
        if(state == State.WAITING_CODE) {
            Buffers.runOnProcess(buffer, codeReader, __ -> state = State.WAITING_PACKET, () -> state = State.ERROR);
        }
        if(state == State.WAITING_PACKET) {
            var tramKind = FrameKind.toTramKind(locationReader.get());
            if (tramKind == null) return ProcessStatus.ERROR;
            var opcode = OpCode.fromByte(codeReader.get());
            if (opcode == null) return ProcessStatus.ERROR;
            try {
                var status = fullReaderFactory.process(buffer, opcode);
                if(status == ProcessStatus.DONE) {
                    state = State.DONE;
                    value = fullReaderFactory.get();
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
        locationReader.reset();
        codeReader.reset();
        fullReaderFactory.reset();
    }
}
