package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.PacketHeader;
import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;

public class HeaderReader implements Reader<PacketHeader>{
    private enum State {
        DONE, WAITING_KIND, WAITING_OP_CODE, ERROR
    }
    private State state = State.WAITING_KIND;
    private final ByteReader reader = new ByteReader();
    private PacketHeader value;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(Byte.BYTES * 2);
    @Override
    public ProcessStatus process(ByteBuffer bb) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }
        if(state == State.WAITING_KIND) {
            var status = reader.process(bb);
            if(status == ProcessStatus.DONE) {
                internalBuffer.put(reader.get());
                state = State.WAITING_OP_CODE;
                reader.reset();
            }
        }
        if(state == State.WAITING_OP_CODE) {
            var status = reader.process(bb);
            if(status == ProcessStatus.DONE) {
                internalBuffer.put(reader.get());
                state = State.DONE;
            }
        }
        if(state == State.DONE) {
            internalBuffer.flip();
            var kindCode = internalBuffer.get();
            TramKind kind = switch (kindCode) {
                case 0x00 -> TramKind.LOCAL;
                case 0x01 -> TramKind.BROADCAST;
                case 0x02 -> TramKind.TRANSFERT;
                default -> TramKind.ANY;
            };
            value = new PacketHeader(kind, internalBuffer.get());
        }
        if (state != State.DONE) {
            return ProcessStatus.REFILL;
        }
        return ProcessStatus.DONE;
    }

    @Override
    public PacketHeader get() {
        if(state != State.DONE){
            throw new IllegalStateException();
        }
        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_KIND;
        reader.reset();
        internalBuffer.clear();
    }
}
