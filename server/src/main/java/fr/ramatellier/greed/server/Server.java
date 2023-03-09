package fr.ramatellier.greed.server;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final int BUFFER_SIZE = 1_024;
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;
    private final boolean isRoot;

    private Server(int port) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        selector = Selector.open();
        isRoot = true;
    }

    private Server(int hostPort, String IP, int connectPort) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(hostPort));
        selector = Selector.open();
        isRoot = false;
    }

    private static Server createROOT(int port) throws IOException {
        return new Server(port);
    }

    private static Server createCONNECTED(int hostPort, String IP, int connectPort) throws IOException {
        return new Server(hostPort, IP, connectPort);
    }

    public void launch() throws IOException {
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (!Thread.interrupted()) {
            Helpers.printKeys(selector); // for debug
            System.out.println("Starting select");
            try {
                selector.select(this::treatKey);
            } catch (UncheckedIOException tunneled) {
                throw tunneled.getCause();
            }
            System.out.println("Select finished");
        }
    }

    private void treatKey(SelectionKey key) {
        Helpers.printSelectedKey(key); // for debug
        try {
            if (key.isValid() && key.isAcceptable()) {
                doAccept(key);
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        try {
            if (key.isValid() && key.isWritable()) {
                ((Context) key.attachment()).doWrite();
            }
            if (key.isValid() && key.isReadable()) {
                ((Context) key.attachment()).doRead();
            }
        } catch (IOException e) {
            logger.log(Level.INFO, "Connection closed with client due to IOException", e);
            silentlyClose(key);
        }
    }

    private void doAccept(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel sc = ssc.accept();

        if (sc == null) {
            return;
        }

        sc.configureBlocking(false);
        var socketKey = sc.register(selector, SelectionKey.OP_READ);
        socketKey.attach(new Context(this, socketKey));
    }

    private void silentlyClose(SelectionKey key) {
        Channel sc = (Channel) key.channel();
        try {
            sc.close();
        } catch (IOException e) {
            // ignore exception
        }
    }

    private void broadcast(Packet packet) {
    }

    public static void main(String[] args) throws NumberFormatException, IOException {
        if (args.length != 1 && args.length != 3) {
            usage();
            return;
        }

        if(args.length == 1) {
            createROOT(Integer.parseInt(args[0])).launch();
        }
        else {
            createCONNECTED(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2])).launch();
        }
    }

    private static void usage() {
        System.out.println("Usage (ROOT MODE) : Server port");
        System.out.println("Usage (CONNECTED MODE) : Server port IP port");
    }
}
