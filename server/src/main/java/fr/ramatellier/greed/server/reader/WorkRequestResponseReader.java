package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.IDPacket;
import fr.ramatellier.greed.server.packet.WorkRequestResponse;

import java.nio.ByteBuffer;

public class WorkRequestResponseReader implements Reader<WorkRequestResponse>{

    private enum State{
        ERROR,
        WAITING_SRC_ID,
        WAITING_DST_ID,
        WAITING_REQUEST_ID,
        WAITING_UC,
        DONE,
        REFILL,
    }
    private IDPacket src;
    private IDPacket dst;
    private long requestID;
    private long nb_uc;
    private final IDReader idReader = new IDReader();
    private final LongReader longReader = new LongReader();
    private WorkRequestResponse workRequestResponse;
    private State state = State.WAITING_SRC_ID;
    @Override
    public ProcessStatus process(ByteBuffer bb) {
        if(state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }
        if(state == State.WAITING_DST_ID) {
            var status = idReader.process(bb);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_SRC_ID;
                idReader.reset();
            } else if(status == ProcessStatus.ERROR) {
                return ProcessStatus.ERROR;
            }
        }
        if(state == State.WAITING_SRC_ID) {
            var status = idReader.process(bb);
            if(status == ProcessStatus.DONE) {
                state = State.WAITING_REQUEST_ID;
            } else if(status == ProcessStatus.ERROR) {
                return ProcessStatus.ERROR;
            }
        }
        if(state == State.WAITING_REQUEST_ID) {
            var status = longReader.process(bb);
            if(status == ProcessStatus.DONE) {
                state = State.WAITING_UC;
            } else if(status == ProcessStatus.ERROR) {
                return ProcessStatus.ERROR;
            }
        }
        if(state == State.WAITING_UC) {
            var status = longReader.process(bb);
            if(status == ProcessStatus.DONE) {
                state = State.DONE;
                workRequestResponse = new WorkRequestResponse(idReader.get(), idReader.get(), longReader.get(), longReader.get());
            } else if(status == ProcessStatus.ERROR) {
                return ProcessStatus.ERROR;
            }
        }
        if(state != State.DONE) {
            return ProcessStatus.REFILL;
        }
        return ProcessStatus.DONE;
    }

    @Override
    public WorkRequestResponse get() {
        return workRequestResponse;
    }

    @Override
    public void reset() {
        idReader.reset();
        longReader.reset();
        workRequestResponse = null;
    }
}
