package fr.ramatellier.greed.server.util.http;

import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.util.Buffers;

import java.nio.ByteBuffer;

public class HTTPReader implements Reader<byte[]> {
    private final HTTPHeaderReader headerReader = new HTTPHeaderReader();
    private ByteBuffer bodyBuffer;
    private byte[] body;
    private State state = State.WAITING_HEADER;
    private enum State {
        DONE, WAITING_HEADER, WAITING_BODY, ERROR
    }

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if(state == State.DONE || state == State.ERROR){
            throw new IllegalStateException();
        }
        if(state == State.WAITING_HEADER){
            var status = headerReader.process(buffer);
            if(status == ProcessStatus.DONE) {
                state = State.WAITING_BODY;
            }
            else if(status == ProcessStatus.REFILL){
                return ProcessStatus.REFILL;
            }
        }
        if(state == State.WAITING_BODY){
            var header = headerReader.get();
            if(header.getCode() != 200){
                state = State.ERROR;
                return ProcessStatus.ERROR;
            }
            try {
                System.out.println(header);
                var contentLength = header.getContentLength();
                bodyBuffer = ByteBuffer.allocate(contentLength);
                if(contentLength == 0){
                    state = State.DONE;
                    return ProcessStatus.DONE;
                }
                buffer.flip();
                bodyBuffer.put(buffer);
                if(bodyBuffer.position() < contentLength){
                    buffer.compact();
                    return ProcessStatus.REFILL;
                }
                body = new byte[contentLength];
                Buffers.fillBuffer(buffer, bodyBuffer);
//                bodyBuffer.put(buffer);

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