package fr.ramatellier.greed.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

public class ServerApplicationContext extends Context {
    private static final Logger logger = Logger.getLogger(ServerApplicationContext.class.getName());

    public ServerApplicationContext(Server server, SelectionKey key) {
        super(server, key);
    }

    public void doAccept(SelectionKey key, Selector selector, Server server) throws IOException {
        logger.info("Accepting connection...");
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel sc = ssc.accept();

        if (sc == null) {
            return;
        }

        sc.configureBlocking(false);
        var socketKey = sc.register(selector, SelectionKey.OP_READ);
        socketKey.attach(new ServerApplicationContext(server, socketKey));
    }
}
