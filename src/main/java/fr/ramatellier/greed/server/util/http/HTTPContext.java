package fr.ramatellier.greed.server.util.http;

import fr.ramatellier.greed.server.Context;
import fr.ramatellier.greed.server.reader.Reader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Objects;
import java.util.logging.Logger;

public final class HTTPContext extends Context {
    private final NonBlockingHTTPJarProvider client;
    private final String request;
    private boolean isRequestSent;
    private final HTTPReader reader = new HTTPReader();
    private static final Logger LOGGER = Logger.getLogger(HTTPContext.class.getName());

    HTTPContext(NonBlockingHTTPJarProvider client, SelectionKey key, String request) {
        super(null, key);
        this.client = Objects.requireNonNull(client);
        this.request = Objects.requireNonNull(request);
    }

    private void silentlyClose() {
        try {
            sc.close();
        } catch (IOException e) {
            // ignore exception
        }
    }

    public void doConnect() throws IOException {
        if(!sc.finishConnect()){
            return;
        }
        key.interestOps(SelectionKey.OP_WRITE);
    }

    @Override
    protected void processIn() {
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

    @Override
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

    @Override
    protected void processOut() {
        bufferOut.put(request.getBytes());
        isRequestSent = true;
    }

}