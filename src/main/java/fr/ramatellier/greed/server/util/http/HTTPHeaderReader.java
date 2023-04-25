package fr.ramatellier.greed.server.util.http;

import fr.ramatellier.greed.server.reader.Reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;


public class HTTPHeaderReader implements Reader<HTTPHeader> {

    private enum State {
        DONE, WAITING_STATUS, WAITING_PAYLOADS, ERROR
    }
    private State state = State.WAITING_STATUS;
    private HTTPHeader header;
    private final StringBuilder builder = new StringBuilder();
    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        try {
            var map = new HashMap<String, String>();
            String contentHeader = read(buffer);
            if(contentHeader == null || contentHeader.isEmpty()){
                System.out.println("Refill header");
                return ProcessStatus.REFILL;
            }
            var response = Arrays.stream(contentHeader.split("\n")).toArray(String[]::new)[0];
            var lines = Arrays.stream(contentHeader.split("\n")).skip(1).toArray(String[]::new);
            for(var line: lines) {
                int index = 0;
                for (; line.charAt(index) != ':'; index++) ;
                String key = line.substring(0, index);
                String value = line.substring(index + 1);
                var split = line.split(": ");
                if (split.length != 2) {
                    throw new HTTPException("Response is ill-formed");
                }
                map.merge(key, value, (newValue, old) -> String.join(";", old, newValue));
            }
            header = HTTPHeader.create(response, map);
            state = State.DONE;
            return ProcessStatus.DONE;
        }catch (IOException e) {
            return ProcessStatus.ERROR;
        }
    }

    @Override
    public HTTPHeader get() {
        if(state != State.DONE){
            throw new IllegalStateException("Cannot get result before process is done");
        }
        return header;
    }

    @Override
    public void reset() {
        state = State.WAITING_STATUS;
        builder.setLength(0);
    }

    String read(ByteBuffer buffer) {
        byte b = 0;
        buffer.flip();
        var done = false;
        while(buffer.hasRemaining()){
            b = buffer.get();
            builder.append((char) b);
            if (builder.length() > 3 && builder.substring(builder.length() - 4).equals("\r\n\r\n")) {
                done = true;
                break;
            }
        }
        if(!done){ //This case occurs when the buffer has end its reading state but the header is not complete
            return null;
        }
        buffer.compact();
        return builder.substring(0, builder.length() - 4);
    }

}
