package fr.ramatellier.greed.server.reader.full;

import fr.ramatellier.greed.server.packet.full.WorkResponsePacket;
import fr.ramatellier.greed.server.reader.FullPacketReader;
import fr.ramatellier.greed.server.reader.sub.IDReader;
import fr.ramatellier.greed.server.reader.sub.ResponsePacketReader;
import fr.ramatellier.greed.server.reader.primitive.LongReader;

import java.nio.ByteBuffer;

public class WorkResponsePacketReader implements FullPacketReader {
    enum State {
        DONE, WAITING_ID_SRC, WAITING_ID_DST, WAITING_REQUEST_ID, WAITING_RESPONSE, ERROR
    }
    private State state = State.WAITING_ID_SRC;
    private final IDReader idSrcReader = new IDReader();
    private final IDReader idDstReader = new IDReader();
    private final LongReader requestIdReader = new LongReader();
    private final ResponsePacketReader responsePacketReader = new ResponsePacketReader();
    private WorkResponsePacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if(state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_ID_SRC) {
            var status = idSrcReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_ID_DST;
            }
        }
        if(state == State.WAITING_ID_DST) {
            var status = idDstReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_REQUEST_ID;
            }
        }
        if(state == State.WAITING_REQUEST_ID) {
            var status = requestIdReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_RESPONSE;
            }
        }
        if(state == State.WAITING_RESPONSE) {
            var status = responsePacketReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.DONE;

                value = new WorkResponsePacket(idSrcReader.get(), idDstReader.get(), requestIdReader.get(), responsePacketReader.get());
            }
        }

        if(state != State.DONE) {
            return ProcessStatus.REFILL;
        }

        return ProcessStatus.DONE;
    }

    @Override
    public WorkResponsePacket get() {
        if(state != State.DONE) {
            throw new IllegalStateException();
        }

        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_ID_SRC;
        idSrcReader.reset();
        idDstReader.reset();
        requestIdReader.reset();
        responsePacketReader.reset();
    }
}
