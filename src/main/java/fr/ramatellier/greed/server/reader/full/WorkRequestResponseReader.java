package fr.ramatellier.greed.server.reader.full;

import fr.ramatellier.greed.server.packet.full.WorkRequestResponsePacket;
import fr.ramatellier.greed.server.packet.sub.LongPacketPart;
import fr.ramatellier.greed.server.reader.FullPacketReader;
import fr.ramatellier.greed.server.reader.sub.IDReader;
import fr.ramatellier.greed.server.reader.primitive.LongReader;

import java.nio.ByteBuffer;

public class WorkRequestResponseReader implements FullPacketReader {
    private enum State {
        DONE, WAITING_ID_DST, WAITING_ID_SRC, WAITING_REQUEST_ID, WAITING_UC, ERROR
    }
    private State state = State.WAITING_ID_DST;
    private final IDReader idDstReader = new IDReader();
    private final IDReader idSrcReader = new IDReader();
    private final LongReader requestIDReader = new LongReader();
    private final LongReader ucReader = new LongReader();
    private WorkRequestResponsePacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if(state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_ID_DST) {
            var status = idDstReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_ID_SRC;
            }
        }
        if(state == State.WAITING_ID_SRC) {
            var status = idSrcReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_REQUEST_ID;
            }
        }
        if(state == State.WAITING_REQUEST_ID) {
            var status = requestIDReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_UC;
            }
        }
        if(state == State.WAITING_UC) {
            var status = ucReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.DONE;

                value = new WorkRequestResponsePacket(idDstReader.get(), idSrcReader.get(), new LongPacketPart(requestIDReader.get()), new LongPacketPart(ucReader.get()));
            }
        }

        if(state != State.DONE) {
            return ProcessStatus.REFILL;
        }

        return ProcessStatus.DONE;
    }

    @Override
    public WorkRequestResponsePacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }

        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_ID_DST;
        idDstReader.reset();
        idSrcReader.reset();
        requestIDReader.reset();
        ucReader.reset();
    }
}
