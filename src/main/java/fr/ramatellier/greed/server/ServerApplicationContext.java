package fr.ramatellier.greed.server;

import java.nio.channels.SelectionKey;
import java.util.logging.Logger;

public class ServerApplicationContext extends Context {
    private static final Logger logger = Logger.getLogger(ServerApplicationContext.class.getName());

    public ServerApplicationContext(Server server, SelectionKey key) {
        super(server, key);
    }

}
