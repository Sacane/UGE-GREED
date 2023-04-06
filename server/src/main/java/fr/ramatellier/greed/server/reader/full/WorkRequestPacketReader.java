package fr.ramatellier.greed.server.reader.full;

import fr.ramatellier.greed.server.packet.full.WorkRequestPacket;
import fr.ramatellier.greed.server.reader.FullPacketReader;
import fr.ramatellier.greed.server.reader.sub.IDReader;
import fr.ramatellier.greed.server.reader.sub.RangePacketReader;
import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.primitive.LongReader;
import fr.ramatellier.greed.server.reader.sub.CheckerPacketReader;

import java.nio.ByteBuffer;

public class WorkRequestPacketReader implements FullPacketReader {
    private enum State {
        DONE, WAITING_ID_SRC, WAITING_ID_DST, WAITING_REQUEST_ID, WAITING_CHECKER, WAITING_RANGE, WAITING_MAX, ERROR
    }
    private State state = State.WAITING_ID_SRC;
    private final IDReader idSrcReader = new IDReader();
    private final IDReader idDstReader = new IDReader();
    private final LongReader requestIdReader = new LongReader();
    private final CheckerPacketReader checkerPacketReader = new CheckerPacketReader();
    private final RangePacketReader rangePacketReader = new RangePacketReader();
    private final LongReader maxReader = new LongReader();
    private WorkRequestPacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
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
                state = State.WAITING_CHECKER;
            }
        }
        if(state == State.WAITING_CHECKER) {
            var status = checkerPacketReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_RANGE;
            }
        }
        if(state == State.WAITING_RANGE) {
            var status = rangePacketReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_MAX;
            }
        }
        if(state == State.WAITING_MAX) {
            var status = maxReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.DONE;

                value = new WorkRequestPacket(idSrcReader.get().getSocket(), idDstReader.get().getSocket(), requestIdReader.get(), checkerPacketReader.get().url(), checkerPacketReader.get().className(), rangePacketReader.get().start(), rangePacketReader.get().end(), maxReader.get());
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
        state = State.WAITING_ID_SRC;
        idSrcReader.reset();
        idDstReader.reset();
        requestIdReader.reset();
        checkerPacketReader.reset();
        rangePacketReader.reset();
        maxReader.reset();
    }
}
