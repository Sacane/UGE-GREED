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
    private String status;
    private State state = State.WAITING_STATUS;
    private HTTPHeader header;
    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        try {
            var map = new HashMap<String, String>();
            if(!buffer.hasRemaining()){
                return ProcessStatus.REFILL;
            }
            String contentHeader = readLineCRLF(buffer);
            System.out.println("CONTENT " + contentHeader + " END CONTENT");
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
            System.out.println(map);
            header = HTTPHeader.create(response, map);
            state = State.DONE;
            return ProcessStatus.DONE;
        }catch (IOException e) {
            return ProcessStatus.ERROR;
        }
//        try {
//            if (state == State.DONE || state == State.ERROR) {
//                System.out.println("??");
//                return ProcessStatus.ERROR;
//            }
//            if (state == State.WAITING_STATUS) {
//                var statusCode = readLineCRLF(buffer);
//                if (statusCode.isEmpty()) {
//                    return ProcessStatus.REFILL;
//                }
//                state = State.WAITING_PAYLOADS;
//                status = statusCode;
//            }
//            if(state == State.WAITING_PAYLOADS){
//                var map = new HashMap<String, String>();
//                var content = readLineCRLF(buffer);
//                if(content.isEmpty()){
//                    return ProcessStatus.REFILL;
//                }
//                var lines = Arrays.stream(content.split("\n")).toArray(String[]::new);
//                for(var line: lines) {
//                    int index = 0;
//                    if(line.isEmpty()){
//                        return ProcessStatus.REFILL;
//                    }
//                    for (; line.charAt(index) != ':'; index++) ;
//                    String key = line.substring(0, index);
//                    String value = line.substring(index + 1);
//                    var split = line.split(": ");
////                    if (split.length != 2) {
////                        throw new HTTPException("Response is ill-formed");
////                    }
//                    map.merge(key, value, (newValue, old) -> String.join(";", old, newValue));
//                }
//                header = HTTPHeader.create(status, map);
//                state = State.DONE;
//                return ProcessStatus.DONE;
//            }
//            return ProcessStatus.REFILL;
//        }catch (IOException e){
//            System.out.println("??????");
//            return ProcessStatus.ERROR;
//        }
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
        status = null;
    }

    String readLineCRLF(ByteBuffer buffer) {
        var builder = new StringBuilder();
        byte b = 0;
        buffer.flip();
        while(buffer.hasRemaining()){
            b = buffer.get(); // lit un octet du buffer
            builder.append((char) b); // ajoute l'octet au header
            if (builder.length() > 3 && builder.substring(builder.length() - 4).equals("\r\n\r\n")) {
                // fin des en-têtes
                break;
            }
        }
        buffer.compact();
        return builder.substring(0, builder.length() - 4);
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
