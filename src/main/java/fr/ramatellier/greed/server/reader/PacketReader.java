package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.frame.Frame;
import fr.ramatellier.greed.server.reader.primitive.ByteReader;
import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.FrameKind;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

public class PacketReader implements FullPacketReader {
    private enum State {
        DONE, WAITING_LOCATION, WAITING_CODE, WAITING_PACKET, ERROR
    }
    private final PacketReaderAdapter fullReaderFactory = new PacketReaderAdapter();
    private State state = State.WAITING_LOCATION;
    private final ByteReader locationReader = new ByteReader();
    private final ByteReader codeReader = new ByteReader();

    private Frame value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        System.out.println("PacketReader state : " + state);
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_LOCATION) {
            var status = locationReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_CODE;
            }
        }
        if(state == State.WAITING_CODE) {
            var status = codeReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_PACKET;
            }
        }
        if(state == State.WAITING_PACKET) {
            var tramKind = FrameKind.toTramKind(locationReader.get());
            if (tramKind == null) return ProcessStatus.ERROR;
            var opcode = OpCodes.fromByte(codeReader.get());
            if (opcode == null) return ProcessStatus.ERROR;
            try {
                var status = fullReaderFactory.process(buffer, opcode);
                if(status == ProcessStatus.DONE) {
                    state = State.DONE;
                    value = fullReaderFactory.get();
                }
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                System.err.println("Error while creating reader for opcode " + opcode + " : " + e.getMessage());
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
