package fr.ramatellier.greed.server.util.http;

import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.Buffers;

import java.nio.ByteBuffer;

public class HTTPReader implements Reader<byte[]> {
    private final HTTPHeaderReader headerReader = new HTTPHeaderReader();
    private ByteBuffer bodyBuffer;
    private byte[] body;
    private State state = State.WAITING_HEADER;
    private enum State {
        DONE, WAITING_HEADER, WAITING_BODY, REFILL, ERROR
    }

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if(state == State.DONE || state == State.ERROR){
            throw new IllegalStateException();
        }
        if(state == State.WAITING_HEADER){
            Buffers.runOnProcess(buffer, headerReader, header -> {
                state = State.WAITING_BODY;
                try {
                    bodyBuffer = ByteBuffer.allocate(header.getContentLength());
                }catch (HTTPException e){
                    state = State.ERROR;
                }
            }, () -> state = State.ERROR);
            if(state == State.REFILL){
                return ProcessStatus.REFILL;
            }
            else if(state == State.ERROR){
                return ProcessStatus.ERROR;
            }

        }
        if(state == State.WAITING_BODY){
            var header = headerReader.get();
            if(header.getCode() != 200){
                state = State.ERROR;
                return ProcessStatus.ERROR;
            }
            try {
                var contentLength = header.getContentLength();

                if(contentLength == 0){
                    state = State.DONE;
                    return ProcessStatus.DONE;
                }
                Buffers.fillBuffer(buffer, bodyBuffer);
                if(bodyBuffer.hasRemaining()){
                    return ProcessStatus.REFILL;
                }
                body = new byte[contentLength];
                bodyBuffer.flip();
                bodyBuffer.get(body);
                bodyBuffer.compact();
                state = State.DONE;
                buffer.compact();
            }catch (HTTPException e){
                state = State.ERROR;
                return ProcessStatus.ERROR;
            }
        }
        if(state == State.DONE){
            return ProcessStatus.DONE;
        }
        return ProcessStatus.REFILL;
    }

    @Override
    public byte[] get() {
        if(state != State.DONE){
            throw new IllegalStateException();
        }
        return body;
    }

    @Override
    public void reset() {
        state = State.WAITING_HEADER;
        headerReader.reset();
        bodyBuffer = null;

    }
}