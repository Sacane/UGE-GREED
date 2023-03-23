package fr.ramatellier.greed.server.packet;

import java.nio.ByteBuffer;
import java.util.Objects;

public final class ResponsePacket implements Packet{
    private final byte responseCode;
    private final StringPacket response;
    private final long value;
    public ResponsePacket(long value, String response, byte responseCode) {
        Objects.requireNonNull(response);
        this.response = new StringPacket(response);
        this.responseCode = responseCode;
        this.value = value;
    }
    @Override
    public void putInBuffer(ByteBuffer buffer) {
        buffer.putLong(value);
        buffer.put(responseCode);
        if(responseCode == 0){
            response.putInBuffer(buffer);
        }
    }
}
