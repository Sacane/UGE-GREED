package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.ResponsePacket;

import java.nio.ByteBuffer;

public class ResponsePacketReader implements Reader<ResponsePacket>{
    private final LongReader longReader = new LongReader();
    private final ByteReader byteReader = new ByteReader();
    private final StringReader stringReader = new StringReader();

    enum State {
        ERROR,
        WAITING_VALUE,
        WAITING_RESPONSE_CODE,
        WAITING_RESPONSE,
        DONE,
    }
    private State state = State.WAITING_VALUE;
    @Override
    public ProcessStatus process(ByteBuffer bb) {
        if(state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }
        if(state == State.WAITING_VALUE) {
            var status = longReader.process(bb);
            if(status == ProcessStatus.DONE) {
                state = State.WAITING_RESPONSE_CODE;
            } else if(status == ProcessStatus.ERROR) {
                return ProcessStatus.ERROR;
            }
        }
        if(state == State.WAITING_RESPONSE_CODE) {
            var status = byteReader.process(bb);
            if(status == ProcessStatus.DONE) {
                state = State.WAITING_RESPONSE;
            } else if(status == ProcessStatus.ERROR) {
                return ProcessStatus.ERROR;
            }
        }
        if(state == State.WAITING_RESPONSE) {
            var status = stringReader.process(bb);
            if(status == ProcessStatus.DONE) {
                state = State.DONE;
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
    public ResponsePacket get() {
        if(state != State.DONE) {
            throw new IllegalStateException();
        }
        return new ResponsePacket(longReader.get(), stringReader.get(), byteReader.get());
    }

    @Override
    public void reset() {
        state = State.WAITING_VALUE;
        longReader.reset();
        byteReader.reset();
        stringReader.reset();
    }
}
