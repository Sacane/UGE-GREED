package fr.ramatellier.greed.server.reader.full;

import fr.ramatellier.greed.server.packet.full.WorkAssignmentPacket;
import fr.ramatellier.greed.server.reader.FullPacketReader;
import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.primitive.LongReader;
import fr.ramatellier.greed.server.reader.sub.DestinationPacketReader;
import fr.ramatellier.greed.server.reader.sub.RangePacketReader;

import java.nio.ByteBuffer;

public class WorkAssignmentPacketReader implements FullPacketReader {
    private enum State {
        DONE, WAITING_DESTINATION, WAITING_REQUEST_ID, WAITING_RANGES, ERROR
    }
    private State state = State.WAITING_DESTINATION;
    private final DestinationPacketReader destinationPacketReader = new DestinationPacketReader();
    private final LongReader requestIdReader = new LongReader();
    private final RangePacketReader rangePacketReader = new RangePacketReader();
    private WorkAssignmentPacket value;

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
                state = State.WAITING_RANGES;
            }
        }
        if(state == State.WAITING_RANGES) {
            var status = rangePacketReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.DONE;

                value = new WorkAssignmentPacket(destinationPacketReader.get().getIdSrc(), destinationPacketReader.get().getIdDst(), requestIdReader.get(), rangePacketReader.get());
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
        state = State.WAITING_DESTINATION;
        destinationPacketReader.reset();
        requestIdReader.reset();
        rangePacketReader.reset();
    }
}
