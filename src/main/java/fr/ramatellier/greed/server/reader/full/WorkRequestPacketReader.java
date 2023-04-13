package fr.ramatellier.greed.server.reader.full;

import fr.ramatellier.greed.server.packet.full.WorkRequestPacket;
import fr.ramatellier.greed.server.packet.sub.LongPacketPart;
import fr.ramatellier.greed.server.reader.FullPacketReader;
import fr.ramatellier.greed.server.reader.sub.DestinationPacketReader;
import fr.ramatellier.greed.server.reader.sub.RangePacketReader;
import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.primitive.LongReader;
import fr.ramatellier.greed.server.reader.sub.CheckerPacketReader;

import java.nio.ByteBuffer;

public class WorkRequestPacketReader implements FullPacketReader {
    private enum State {
        DONE, WAITING_DESTINATION, WAITING_REQUEST_ID, WAITING_CHECKER, WAITING_RANGE, WAITING_MAX, ERROR
    }
    private State state = State.WAITING_DESTINATION;
    private final DestinationPacketReader destinationPacketReader = new DestinationPacketReader();
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

        if(state == State.WAITING_DESTINATION) {
            var status = destinationPacketReader.process(buffer);

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

                value = new WorkRequestPacket(destinationPacketReader.get().getIdSrc(),
                        destinationPacketReader.get().getIdDst(),
                        new LongPacketPart(requestIdReader.get()), checkerPacketReader.get(), rangePacketReader.get(), new LongPacketPart(maxReader.get()));
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
        state = State.WAITING_DESTINATION;
        destinationPacketReader.reset();
        requestIdReader.reset();
        checkerPacketReader.reset();
        rangePacketReader.reset();
        maxReader.reset();
    }
}
