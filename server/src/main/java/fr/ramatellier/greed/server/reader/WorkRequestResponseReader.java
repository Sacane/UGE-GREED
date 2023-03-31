package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.IDPacket;
import fr.ramatellier.greed.server.packet.WorkRequestResponsePacket;

import java.nio.ByteBuffer;

public class WorkRequestResponseReader implements Reader<WorkRequestResponsePacket>{

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
    private final IDReader idReader = new IDReader();
    private final LongReader longReader = new LongReader();
    private WorkRequestResponsePacket workRequestResponsePacket;
    private State state = State.WAITING_DST_ID;
    @Override
    public ProcessStatus process(ByteBuffer bb) {
        if(state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }
        if(state == State.WAITING_DST_ID) {
            var status = idReader.process(bb);
            if(status == ProcessStatus.DONE) {
                state = State.WAITING_SRC_ID;
                dst = idReader.get();
                idReader.reset();
            } else if(status == ProcessStatus.ERROR) {
                return ProcessStatus.ERROR;
            }
        }
        if(state == State.WAITING_SRC_ID) {
            var status = idReader.process(bb);
            if(status == ProcessStatus.DONE) {
                state = State.WAITING_REQUEST_ID;
                src = idReader.get();
            } else if(status == ProcessStatus.ERROR) {
                return ProcessStatus.ERROR;
            }
        }
        if(state == State.WAITING_REQUEST_ID) {
            var status = longReader.process(bb);
            if(status == ProcessStatus.DONE) {
                state = State.WAITING_UC;
                requestID = longReader.get();
                longReader.reset();
            } else if(status == ProcessStatus.ERROR) {
                return ProcessStatus.ERROR;
            }
        }
        if(state == State.WAITING_UC) {
            var status = longReader.process(bb);
            if(status == ProcessStatus.DONE) {
                state = State.DONE;
                var nb_uc = longReader.get();
                workRequestResponsePacket = new WorkRequestResponsePacket(dst, src, requestID, nb_uc);
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
    public WorkRequestResponsePacket get() {
        return workRequestResponsePacket;
    }

    @Override
    public void reset() {
        idReader.reset();
        longReader.reset();
        workRequestResponsePacket = null;
        state = State.WAITING_DST_ID;
    }
}
