package fr.ramatellier.greed.server.packet.sub;

import fr.ramatellier.greed.server.packet.Packet;

import java.nio.ByteBuffer;

public final class ResponsePacket implements Packet {
    private final byte responseCode;
    private final StringPacket response;
    private final long value;

    /**
     *
     * @param value
     * @param response
     * @param responseCode
     */
    public ResponsePacket(long value, String response, byte responseCode) {
        this.response = (response != null) ? new StringPacket(response) : null;
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

    @Override
    public int size() {
        return Long.BYTES + Byte.BYTES + response.size();
    }

    public byte getResponseCode() {
        return responseCode;
    }

    public StringPacket getResponse() {
        return response;
    }

    public long getValue() {
        return value;
    }
}