package fr.ramatellier.greed.server.reader.full;

import fr.ramatellier.greed.server.packet.sub.RangePacket;
import fr.ramatellier.greed.server.packet.full.WorkAssignmentPacket;
import fr.ramatellier.greed.server.reader.sub.IDReader;
import fr.ramatellier.greed.server.reader.sub.RangePacketReader;
import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.primitive.IntReader;
import fr.ramatellier.greed.server.reader.primitive.LongReader;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class WorkAssignmentPacketReader implements Reader<WorkAssignmentPacket> {
    private enum State {
        DONE, WAITING_IDSRC, WAITING_IDDST, WAITING_REQUESTID, WAITING_SIZE, WAITING_RANGES, ERROR
    }
    private State state = State.WAITING_IDSRC;
    private final IDReader idSrcReader = new IDReader();
    private final IDReader idDstReader = new IDReader();
    private final LongReader requestIdReader = new LongReader();
    private final IntReader sizeReader = new IntReader();
    private final RangePacketReader rangePacketReader = new RangePacketReader();
    private ArrayList<RangePacket> ranges;
    private WorkAssignmentPacket value;

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
                state = State.WAITING_SIZE;
            }
        }
        if(state == State.WAITING_SIZE) {
            var status = sizeReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_RANGES;
            }
        }
        if(state == State.WAITING_RANGES) {
            if(ranges.size() == sizeReader.get()) {
                state = State.DONE;

                value = new WorkAssignmentPacket(idSrcReader.get().getSocket(), idDstReader.get().getSocket(), requestIdReader.get(), ranges);
            }

            while(buffer.limit() > 0 && ranges.size() != sizeReader.get()) {
                var status = rangePacketReader.process(buffer);

                if(status == ProcessStatus.DONE) {
                    ranges.add(rangePacketReader.get());
                    rangePacketReader.reset();
                }

                if(ranges.size() == sizeReader.get()) {
                    state = State.DONE;

                    value = new WorkAssignmentPacket(idSrcReader.get().getSocket(), idDstReader.get().getSocket(), requestIdReader.get(), ranges);
                }
            }
        }

        if (state != State.DONE) {
            return ProcessStatus.REFILL;
        }

        return Reader.ProcessStatus.DONE;
    }

    @Override
    public WorkAssignmentPacket get() {
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
        requestIdReader.reset();
        sizeReader.reset();
        rangePacketReader.reset();
        ranges = new ArrayList<>();
    }
}
