package fr.ramatellier.greed.server.util.http;

import fr.ramatellier.greed.server.util.Helpers;

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

    public void launch() throws IOException {
        while (!isDone) {
            try {
                Helpers.printKeys(selector);
                selector.select(this::treatKey);
            } catch (UncheckedIOException tunneled) {
                throw tunneled.getCause();
            }
        }
    }

    String resourceTemplate(String resource){
        return "GET " + resource + " HTTP/1.1\r\nHost: " + targetAddress.getHostName() + "\r\n\r\n";
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
            // lambda call in select requires to tunnel IOException
            throw new UncheckedIOException(ioe);
        }
    }

    public static void main(String[] args) throws IOException {
        var client = new HttpClient("http://www-igm.univ-mlv.fr", "GET /~carayol/Factorizer.jar HTTP/1.1\r\nHost: igm.univ-mlv.fr\r\n\r\n");
        client.launch();
    }
}
