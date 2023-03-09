package fr.ramatellier.greed.server.reader;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static fr.ramatellier.greed.server.reader.Reader.ProcessStatus.DONE;
import static fr.ramatellier.greed.server.reader.Reader.ProcessStatus.REFILL;

public class StringReader implements Reader<String>{

    private enum State {
        DONE, WAITING_INT, WAITING_STRING, ERROR
    }
    private final Charset UTF8 = StandardCharsets.UTF_8;
    private State state = State.WAITING_INT;
    private final static int BUFFER_SIZE = 1024;
    private final IntReader intReader = new IntReader();
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(BUFFER_SIZE); // write-mode
    private String value;

    @Override
    public ProcessStatus process(ByteBuffer bb) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }
        if(state == State.WAITING_INT){
            var status = intReader.process(bb);
            if(status == DONE){
                var size = intReader.get();
                if(size < 0 || size > BUFFER_SIZE){
                    state = State.ERROR;
                    return ProcessStatus.ERROR;
                }
                internalBuffer.limit(size);
                intReader.reset();
                state = State.WAITING_STRING;
            }
        }
        if(state != State.WAITING_STRING){
            return REFILL;
        }
        bb.flip();
        try {
            if (bb.remaining() <= internalBuffer.remaining()) {
                internalBuffer.put(bb);
            } else {
                var oldLimit = bb.limit();
                bb.limit(internalBuffer.remaining());
                internalBuffer.put(bb);
                bb.limit(oldLimit);
            }
        } finally {
            bb.compact();
        }
        if(internalBuffer.hasRemaining()){
            System.out.println("REFILL");
            return REFILL;
        }
        state = State.DONE;
        internalBuffer.flip();
        value = UTF8.decode(internalBuffer).toString();
        return DONE;
    }

    @Override
    public String get() {
        if(state != State.DONE){
            throw new IllegalStateException();
        }
        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_INT;
        internalBuffer.clear();
        intReader.reset();
    }
}