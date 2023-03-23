package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.IDPacket;
import fr.ramatellier.greed.server.packet.ResponsePacket;

import java.nio.ByteBuffer;

public class ResponsePacketReader implements Reader<ResponsePacket>{
    private String response;
    private Long value;
    private Byte responseCode;
    private ResponsePacket packet;

    enum State {
        ERROR,
        WAITING_VALUE,
        WAITING_RESPONSE_CODE,
        WAITING_RESPONSE,
        DONE
    }
    private State state = State.WAITING_VALUE;
    @Override
    public ProcessStatus process(ByteBuffer bb) {
        if(state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }
        if(state == State.WAITING_VALUE) {
            if(bb.remaining() >= Long.BYTES) {
                value = bb.getLong();
                state = State.WAITING_RESPONSE_CODE;
            }
        }
        return null;
    }

    @Override
    public ResponsePacket get() {
        return packet;
    }

    @Override
    public void reset() {

    }
}
