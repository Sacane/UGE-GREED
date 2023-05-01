package fr.ramatellier.greed.server.packet.component;

import java.nio.ByteBuffer;

public final class ResponseComponent implements GreedComponent {
    private final byte responseCode;
    private final StringComponent response;
    private final long value;

    /**
     *
     * @param value The value of the long that have been computed
     * @param response The response of the checker
     * @param responseCode The byte that represents the status of the computation
     */
    public ResponseComponent(long value, String response, byte responseCode) {
        this.response = (response != null) ? new StringComponent(response) : null;
        this.responseCode = responseCode;
        this.value = value;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        buffer.putLong(value);
        buffer.put(responseCode);
        response.putInBuffer(buffer);
    }

    @Override
    public int size() {
        return Long.BYTES + Byte.BYTES + response.size();
    }

    public byte getResponseCode() {
        return responseCode;
    }

    public StringComponent getResponse() {
        return response;
    }

    public long getValue() {
        return value;
    }
}
