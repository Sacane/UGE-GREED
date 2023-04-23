package fr.ramatellier.greed.server.util.http;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public final class HttpClient {
    private final SocketChannel sc;
    private final Selector selector;
    private final InetSocketAddress targetAddress;
    private boolean isDone = false;
    private byte[] body;

    public HttpClient(String path, String request) throws IOException {
        var url = new URL(path);
        this.sc = SocketChannel.open();
        this.selector = Selector.open();
        sc.configureBlocking(false);
        this.targetAddress = new InetSocketAddress(url.getHost(), url.getPort() != -1 ? url.getPort() : 80);
        sc.connect(targetAddress);
        var key = sc.register(selector, SelectionKey.OP_CONNECT);
        key.attach(new HttpContext(this, key, request));
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
        while (!isDone) {
            try {
                selector.select(this::treatKey);
            } catch (UncheckedIOException tunneled) {
                throw tunneled.getCause();
            }
        }
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

    public static void main(String[] args) throws IOException {
        var client = new HttpClient("http://www-igm.univ-mlv.fr", "GET /~carayol/Factorizer.jar HTTP/1.1\r\nHost: igm.univ-mlv.fr\r\n\r\n");
        client.launch();
        var filePath = "./Factorizer.jar";
        try(var fos = new FileOutputStream(filePath)){
            fos.write(client.getBody());
            fos.flush();
            System.out.println("File saved at " + filePath);
        }
    }
}
