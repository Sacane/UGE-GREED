package fr.ramatellier.greed.server.util.http;

import fr.ramatellier.greed.server.reader.Reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;


public class HTTPHeaderReader implements Reader<HTTPHeader> {

    private enum State {
        DONE, WAITING_VERSION, WAITING_RESPONSE, WAITING_STATUS, WAITING_PAYLOADS, ERROR
    }
    private String version;
    private String status;
    private State state = State.WAITING_VERSION;
    private HTTPHeader header;
    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        try {
            var map = new HashMap<String, String>();
            if(!buffer.hasRemaining()){
                return ProcessStatus.REFILL;
            }
            String contentHeader = readLineCRLF(buffer);
            if(contentHeader.isEmpty() || !buffer.hasRemaining()){
                return ProcessStatus.REFILL;
            }
            System.out.println("Remaining : " + buffer.remaining());
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
        return header;
    }

    @Override
    public void reset() {
        version = null;
        status = null;
    }

    String readLineCRLF(ByteBuffer buffer) throws IOException {
        var builder = new StringBuilder();
        byte b = 0;
        buffer.flip();
        while(buffer.hasRemaining()){
            byte prev = b;
            b = buffer.get();
            if(prev == '\r' && b == '\n'){
                buffer.compact();
                break;
            }
            builder.append((char)b);
        }
        System.out.println(builder.length());
        System.out.println("DONE");
        return builder.substring(0, builder.length() - 1);
    }
//    public HTTPHeader readHeader(ByteBuffer buffer) throws IOException {
//        var response = readLineCRLF(buffer);
//        var map = new HashMap<String, String>();
//        String line;
//        while(true){
//            line = readLineCRLF(buffer);
//            if(line.isEmpty()){
//                break;
//            }
//            int index = 0;
//            for(;line.charAt(index) != ':';index++);
//            String key = line.substring(0, index);
//            String value = line.substring(index + 1);
//            var split = line.split(": ");
//            if(split.length != 2){
//                throw new HTTPException("Response is ill-formed");
//            }
//            map.merge(key, value, (newValue, old) -> String.join(";", old, newValue));
//        }
//        return HTTPHeader.create(response, map);
//    }

}
