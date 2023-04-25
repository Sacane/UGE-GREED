package fr.ramatellier.greed.server.util.http;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * This class aims to be a simple HTTP client but in non-blocking mode and only perform request to get jar file from a server.
 */
public final class NonBlockingHttpJarProvider {
    private final SocketChannel sc;
    private final Selector selector;
    private boolean isDone = false;
    private byte[] body;
    private final String filePath;
    private Consumer<byte[]> onDone;
    private final Logger LOGGER = Logger.getLogger(NonBlockingHttpJarProvider.class.getName());

    public NonBlockingHttpJarProvider(String path, String request, String filePath) throws IOException {
        this.filePath = Objects.requireNonNullElse(filePath, "result.jar");
        Objects.requireNonNull(path);
        Objects.requireNonNull(request);
        var url = new URL(path);
        this.sc = SocketChannel.open();
        this.selector = Selector.open();
        sc.configureBlocking(false);
        sc.connect(new InetSocketAddress(url.getHost(), url.getPort() != -1 ? url.getPort() : 80));
        var key = sc.register(selector, SelectionKey.OP_CONNECT);
        key.attach(new HttpContext(this, key, request));
    }


    public String getFilePath(){
        return filePath;
    }

    void setBody(byte[] body){
        this.body = body;
        isDone = true;
    }

    public byte[] getBody(){
        if(!isDone){
            throw new IllegalStateException("The request is not done yet");
        }
        return body;
    }

    public void launch() throws IOException {
        LOGGER.info("Launching request to " + sc.getRemoteAddress() + "...");
        while (!isDone) {
            try {
                selector.select(this::treatKey);
            } catch (UncheckedIOException tunneled) {
                throw tunneled.getCause();
            }
        }
        close();
    }

    void executeOnDone(byte[] body){
        this.onDone.accept(body);
    }
    public void onDone(Consumer<byte[]> onDone){
        this.onDone = onDone;
    }
    public boolean isDone(){
        return isDone;
    }
    public void close(){
        try {
            sc.close();
        } catch (IOException ignored) {

        }
    }

    private void treatKey(SelectionKey key) {
        var uniqueContext = (HttpContext) key.attachment();
        try {
            if (key.isValid() && key.isConnectable()) {
                uniqueContext.doConnect();
            }
            if (key.isValid() && key.isWritable()) {
                uniqueContext.doWrite();
            }
            if (key.isValid() && key.isReadable()) {
                uniqueContext.doRead();
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }
    void done() {
        isDone = true;
    }

//    public static void main(String[] args) throws IOException {
//        var filePath = "Factorizer.jar";
//        var client = new NonBlockingHttpJarProvider("http" + "://www-igm.univ-mlv.fr", "GET /~carayol/Factorizer.jar HTTP/1.1\r\nHost: igm.univ-mlv.fr\r\n\r\n", filePath);
//        client.onDone(bytes -> {
//            try(var fos = new FileOutputStream(client.getFilePath())){
//                fos.write(bytes);
//                fos.flush();
//                System.out.println("File saved at " + client.getFilePath());
//            } catch (IOException ignored) {}
//        });
//        client.launch();
//    }
}
