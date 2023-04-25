package fr.ramatellier.greed.server.util.http;

import fr.ramatellier.greed.server.reader.Reader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.logging.Logger;

public final class HttpContext{
    private static final int BUFFER_SIZE = 8192;
    private final ByteBuffer bufferIn = ByteBuffer.allocate(BUFFER_SIZE);
    private final ByteBuffer bufferOut = ByteBuffer.allocate(BUFFER_SIZE);
    private boolean closed = false;
    private final SelectionKey key;
    private final SocketChannel sc;
    private final NonBlockingHttpJarProvider client;
    private final String request;
    private boolean isRequestSent;
    private final HTTPReader reader = new HTTPReader();
    private static final Logger LOGGER = Logger.getLogger(HttpContext.class.getName());

    HttpContext(NonBlockingHttpJarProvider client, SelectionKey key, String request) {
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
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void processIn() {
        while(true){
            var response = reader.process(bufferIn);
            if(response == Reader.ProcessStatus.DONE){
                var contentBody = reader.get();
                try(var fos = new FileOutputStream(client.getFilePath())){
                    fos.write(contentBody);
                    fos.flush();
                    System.out.println("Jar has been received and saved at " + client.getFilePath());
                } catch (IOException e) {
                    LOGGER.warning("Error while writing file");
                    silentlyClose();
                    return;
                }
                client.executeOnDone(contentBody);
                reader.reset();
                client.done();
                break;
            } else if(response == Reader.ProcessStatus.REFILL){
                return;
            } else if(response == Reader.ProcessStatus.ERROR){
                break;
            }
        }
    }

    public void doWrite() throws IOException {
        if(isRequestSent){
            return;
        }
        processOut();
        bufferOut.flip();
        sc.write(bufferOut);
        bufferOut.compact();
        updateInterestOps();
    }

    private void processOut() {
        System.out.println(request.getBytes().length);
        bufferOut.put(request.getBytes());
        isRequestSent = true;
    }

    public void doRead() throws IOException {
        var readValue = sc.read(bufferIn);
        if (readValue == -1) {
            closed = true;
        }
        processIn();
        updateInterestOps();
    }
}