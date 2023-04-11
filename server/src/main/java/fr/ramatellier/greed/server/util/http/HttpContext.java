package fr.ramatellier.greed.server.util.http;

import fr.ramatellier.greed.server.reader.Reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.logging.Logger;

public class HttpContext{
    private static final int BUFFER_SIZE = 8192;
    private final ByteBuffer bufferIn = ByteBuffer.allocate(BUFFER_SIZE);
    private final ByteBuffer bufferOut = ByteBuffer.allocate(BUFFER_SIZE);
    private boolean closed = false;
    private final SelectionKey key;
    private final SocketChannel sc;
    private final HttpClient client;
    private final String request;
    private final HTTPHeaderReader headerReader = new HTTPHeaderReader();
    private static final Logger LOGGER = Logger.getLogger(HttpContext.class.getName());

    HttpContext(HttpClient client, SelectionKey key, String request) {
        Objects.requireNonNull(key);
        this.client = Objects.requireNonNull(client);
        this.key = key;
        this.sc = (SocketChannel) key.channel();
        this.request = request;
    }

    private void silentlyClose() {
        try {
            sc.close();
        } catch (IOException e) {
            // ignore exception
        }
    }
    public void updateInterestOps() {
        var op = 0;

        if (bufferOut.position() > 0) {
            op |= SelectionKey.OP_WRITE;
        }
        if (!closed && bufferIn.hasRemaining()) {
            op |= SelectionKey.OP_READ;
        }
        if (op == 0) {
            silentlyClose();

            return;
        }

        key.interestOps(op);
    }

    public void doConnect() throws IOException {
        if(!sc.finishConnect()){
            return;
        }
        bufferOut.put(request.getBytes());
        key.interestOps(SelectionKey.OP_WRITE);
    }

        private void processIn() throws IOException {
        System.out.println("PROCESS IN WITH -> " + bufferIn.remaining() + " REMAINING");
        while(true){
            var response = headerReader.process(bufferIn);
            if(response == Reader.ProcessStatus.DONE){
                var header = headerReader.get();
                System.out.println("CODE -> " + header.getCode());
                if(header.getCode() == 200){
                    var contentLength = header.getContentLength();
                    System.out.println(header);
                    var body = new byte[contentLength];
                    bufferIn.flip();
                    int toReadLeft = bufferIn.remaining();
                    bufferIn.get(body);
                    while(toReadLeft < contentLength){
                        bufferIn.clear();
                        sc.read(bufferIn);
                        bufferIn.flip();
                        bufferIn.get(body, contentLength - toReadLeft, toReadLeft);
                        contentLength -= toReadLeft;
                        toReadLeft = bufferIn.remaining();
                    }
                    client.setBody(body);
                    System.out.println("BODY GET");
                }
                break;
            } else if(response == Reader.ProcessStatus.REFILL){
                return;
            } else if(response == Reader.ProcessStatus.ERROR){
                System.out.println("ERROR");
                break;
            }
        }
    }

    public void doWrite() throws IOException {
        System.out.println("doWrite");
        bufferOut.flip();
        sc.write(bufferOut);
        bufferOut.compact();
        updateInterestOps();
    }
    public void doRead() {
        System.out.println("doRead");

        try {
            var readValue = sc.read(bufferIn);
            if (readValue == -1) {
                closed = true;
            }
            processIn();
            updateInterestOps();
        } catch (IOException ignored) {
        }
    }
}