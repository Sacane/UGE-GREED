package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.WorkRequestPacket;

import java.nio.ByteBuffer;

public class WorkRequestPacketReader implements Reader<WorkRequestPacket> {
    private enum State {
        DONE, WAITING_IDSRC, WAITING_IDDST, WAITING_REQUESTID, WAITING_CHECKER, ERROR
    }
    private State state = State.WAITING_IDSRC;
    private final IDReader idSrcReader = new IDReader();
    private final IDReader idDstReader = new IDReader();
    private final LongReader requestIdReader = new LongReader();
    private final CheckerPacketReader checkerPacketReader = new CheckerPacketReader();
    private WorkRequestPacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_IDSRC) {
            var status = idSrcReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_IDDST;
            }
        }
        if(state == State.WAITING_IDDST) {
            var status = idDstReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_REQUESTID;
            }
        }
        if(state == State.WAITING_REQUESTID) {
            var status = requestIdReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_CHECKER;
            }
        }
        if(state == State.WAITING_CHECKER) {
            var status = checkerPacketReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.DONE;

                value = new WorkRequestPacket(idSrcReader.get().getSocket(), idDstReader.get().getSocket(), requestIdReader.get(), checkerPacketReader.get().getUrl(), checkerPacketReader.get().getClassName(), 0, 0, 0);
            }
        }

        if (state != State.DONE) {
            return ProcessStatus.REFILL;
        }

        return Reader.ProcessStatus.DONE;
    }

    @Override
    public WorkRequestPacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }

        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_IDSRC;
        idSrcReader.reset();
        idDstReader.reset();
    }
}
