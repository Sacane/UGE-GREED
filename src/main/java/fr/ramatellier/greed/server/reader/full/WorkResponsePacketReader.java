package fr.ramatellier.greed.server.reader.full;

import fr.ramatellier.greed.server.packet.full.WorkResponsePacket;
import fr.ramatellier.greed.server.packet.sub.LongPacketPart;
import fr.ramatellier.greed.server.reader.FullPacketReader;
import fr.ramatellier.greed.server.reader.sub.DestinationPacketReader;
import fr.ramatellier.greed.server.reader.sub.ResponsePacketReader;
import fr.ramatellier.greed.server.reader.primitive.LongReader;

import java.nio.ByteBuffer;

public class WorkResponsePacketReader implements FullPacketReader {
    enum State {
        DONE, WAITING_DESTINATION, WAITING_REQUEST_ID, WAITING_RESPONSE, ERROR
    }
    private State state = State.WAITING_DESTINATION;
    private final DestinationPacketReader destinationPacketReader = new DestinationPacketReader();
    private final LongReader requestIdReader = new LongReader();
    private final ResponsePacketReader responsePacketReader = new ResponsePacketReader();
    private WorkResponsePacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if(state == State.DONE || state == State.ERROR) {
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
                state = State.WAITING_RESPONSE;
            }
        }
        if(state == State.WAITING_RESPONSE) {
            var status = responsePacketReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.DONE;

                value = new WorkResponsePacket(destinationPacketReader.get().getIdSrc(), destinationPacketReader.get().getIdDst(), new LongPacketPart(requestIdReader.get()), responsePacketReader.get());
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
        state = State.WAITING_DESTINATION;
        destinationPacketReader.reset();
        requestIdReader.reset();
        responsePacketReader.reset();
    }
}
