package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.IDPacket;
import fr.ramatellier.greed.server.packet.ResponsePacket;
import fr.ramatellier.greed.server.packet.WorkResponsePacket;

import java.nio.ByteBuffer;

public class WorkResponsePacketReader implements Reader<WorkResponsePacket>{


    enum State {
        ERROR,
        WAITING_SRC_ID,
        WAITING_DST_ID,
        WAITING_REQUEST_ID,
        WAITING_RESPONSE,
        DONE,
    }
    private State state = State.WAITING_SRC_ID;
    private final IDReader srcReader = new IDReader();
    private final IDReader dstReader = new IDReader();
    private final LongReader longReader = new LongReader();
    private final ResponsePacketReader responsePacketReader = new ResponsePacketReader();

    @Override
    public ProcessStatus process(ByteBuffer bb) {
        if(state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }
        if(state == State.WAITING_SRC_ID) {
            var status = srcReader.process(bb);
            if(status == ProcessStatus.DONE) {
                state = State.WAITING_DST_ID;
            } else if(status == ProcessStatus.ERROR) {
                return ProcessStatus.ERROR;
            }
        }
        if(state == State.WAITING_DST_ID) {
            var status = dstReader.process(bb);
            if(status == ProcessStatus.DONE) {
                state = State.WAITING_REQUEST_ID;
            } else if(status == ProcessStatus.ERROR) {
                return ProcessStatus.ERROR;
            }
        }
        if(state == State.WAITING_REQUEST_ID) {
            var status = longReader.process(bb);
            if(status == ProcessStatus.DONE) {
                state = State.WAITING_RESPONSE;
            } else if(status == ProcessStatus.ERROR) {
                return ProcessStatus.ERROR;
            }
        }
        if(state == State.WAITING_RESPONSE) {
            var status = responsePacketReader.process(bb);
            if(status == ProcessStatus.DONE) {
                state = State.DONE;
            } else if(status == ProcessStatus.ERROR) {
                System.out.println("ERROR READING RESPONSE PACKET");
                return ProcessStatus.ERROR;
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
        return new WorkResponsePacket(srcReader.get(), dstReader.get(), longReader.get(), responsePacketReader.get());
    }

    @Override
    public void reset() {
        state = State.WAITING_SRC_ID;
        srcReader.reset();
        dstReader.reset();
        longReader.reset();
        responsePacketReader.reset();
    }
}
