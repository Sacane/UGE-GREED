package fr.ramatellier.greed.server.util.http;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * This class aims to be a simple HTTP client but in non-blocking mode and only to perform request to get jar file from a server.
 */
public final class NonBlockingHTTPJarProvider {
    private final SocketChannel sc;
    private final Selector selector;
    private boolean isDone = false;
    private final String filePath;
    private Consumer<byte[]> onDone;
    private final Logger LOGGER = Logger.getLogger(NonBlockingHTTPJarProvider.class.getName());

    private NonBlockingHTTPJarProvider(String host, String request, String filePath) throws IOException {
        this.filePath = Objects.requireNonNullElse(filePath, "result.jar");
        Objects.requireNonNull(host);
        Objects.requireNonNull(request);
        this.sc = SocketChannel.open();
        this.selector = Selector.open();
        sc.configureBlocking(false);
        sc.connect(new InetSocketAddress(host, 80));
        var key = sc.register(selector, SelectionKey.OP_CONNECT);
        key.attach(new HTTPContext(this, key, request));
    }
    private record HostRequestFile(String host, String request, String file) {}

    public static NonBlockingHTTPJarProvider fromURL(URL url) throws IOException {
        Objects.requireNonNull(url);
        var request = urlToRequest(url);
        System.out.println(request);
        return new NonBlockingHTTPJarProvider(request.host(), request.request(), request.file());
    }
    private static HostRequestFile urlToRequest(URL request){
        var path = request.getPath();
        var host = request.getHost();
        var requestString = "GET " + path + " HTTP/1.1\r\nHost: " + host + "\r\n\r\n";
        return new HostRequestFile(host, requestString, Path.of(path).getFileName().toString());
    }

    public String getFilePath(){
        return filePath;
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

    /**
     * Set the callback to execute when the request is done.
     * @param onDone the callback to execute when the request is done.
     */
    public void onDone(Consumer<byte[]> onDone){
        Objects.requireNonNull(onDone);
        this.onDone = onDone;
    }

    /**
     * @return true if the request is done.
     */
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
        var uniqueContext = (HTTPContext) key.attachment();
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
}
