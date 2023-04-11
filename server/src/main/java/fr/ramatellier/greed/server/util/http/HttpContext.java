package fr.ramatellier.greed.server.util.http;

import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.util.Buffers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;

public class HttpContext{
    private static final int BUFFER_SIZE = 2048;
    private final ByteBuffer bufferIn = ByteBuffer.allocate(BUFFER_SIZE);
    private final ByteBuffer bufferOut = ByteBuffer.allocate(BUFFER_SIZE);
    private static final String CONTENT_LENGTH_HEADER = "Content-Length";
    private ByteBuffer bodyBuffer;
    private boolean closed = false;
    private final SelectionKey key;
    private final SocketChannel sc;
    private final ArrayBlockingQueue<ByteBuffer> queue = new ArrayBlockingQueue<>(2);
    private final HttpClient client;
    private final String request;
    private StringBuilder sb = new StringBuilder();
    private int contentLength;
    private final HTTPHeaderReader headerReader = new HTTPHeaderReader();

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

    private void processIn() {
        while(bufferIn.hasRemaining()){
            var response = headerReader.process(bufferIn);
            if(response == Reader.ProcessStatus.DONE){
                var header = headerReader.get();
                System.out.println("CODE -> " + header.getCode());
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

    private void processOut() {

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
            silentlyClose();
        }
    }
}