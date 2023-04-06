package fr.ramatellier.greed.server.reader.sub;

import fr.ramatellier.greed.server.packet.sub.ResponsePacket;
import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.primitive.ByteReader;
import fr.ramatellier.greed.server.reader.primitive.LongReader;

import java.nio.ByteBuffer;

public class ResponsePacketReader implements Reader<ResponsePacket> {
    enum State {
        DONE, WAITING_VALUE, WAITING_RESPONSE_CODE, WAITING_RESPONSE, ERROR
    }
    private State state = State.WAITING_VALUE;
    private final LongReader valueReader = new LongReader();
    private final ByteReader codeReader = new ByteReader();
    private final StringReader responseReader = new StringReader();
    private ResponsePacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if(state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_VALUE) {
            var status = valueReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_RESPONSE_CODE;
            }
        }
        if(state == State.WAITING_RESPONSE_CODE) {
            var status = codeReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_RESPONSE;
            }
        }
        if(state == State.WAITING_RESPONSE) {
            var status = responseReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.DONE;

                value = new ResponsePacket(valueReader.get(), responseReader.get(), codeReader.get());
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

        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_VALUE;
        valueReader.reset();
        codeReader.reset();
        responseReader.reset();
    }
}
