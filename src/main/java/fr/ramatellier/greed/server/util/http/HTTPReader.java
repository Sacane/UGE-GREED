package fr.ramatellier.greed.server.util.http;

import fr.ramatellier.greed.server.reader.Reader;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HTTPReader implements Reader<HTTPPacket> {

    private final Charset ASCII_CHARSET = StandardCharsets.US_ASCII;

    private final ByteBuffer buffer = ByteBuffer.allocate(1024);
    private enum State {
        DONE, WAITING_HEADER, WAITING_BODY, ERROR
    }

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        return null;
    }

    @Override
    public HTTPPacket get() {
        return null;
    }

    @Override
    public void reset() {

    }
}