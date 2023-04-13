package fr.ramatellier.greed.server.reader.full;

import fr.ramatellier.greed.server.packet.full.DisconnectedPacket;
import fr.ramatellier.greed.server.reader.FullPacketReader;
import fr.ramatellier.greed.server.reader.sub.DestinationPacketReader;
import fr.ramatellier.greed.server.reader.Reader;

import java.nio.ByteBuffer;

public class DisconnectedPacketReader implements FullPacketReader {
    private enum State {
        DONE, WAITING_DESTINATION, ERROR
    }
    private State state = State.WAITING_DESTINATION;
    private final DestinationPacketReader destinationPacketReader = new DestinationPacketReader();
    private DisconnectedPacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_DESTINATION) {
            var status = destinationPacketReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.DONE;

                value = new DisconnectedPacket(destinationPacketReader.get().getIdSrc(), destinationPacketReader.get().getIdDst());
            }
        }

        if (state != State.DONE) {
            return ProcessStatus.REFILL;
        }

        return Reader.ProcessStatus.DONE;
    }

    @Override
    public DisconnectedPacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }

        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_DESTINATION;
        destinationPacketReader.reset();
    }
}
