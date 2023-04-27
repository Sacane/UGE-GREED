package fr.ramatellier.greed.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public class ClientApplicationContext extends Context {
    public ClientApplicationContext(Server server, SelectionKey key) {
        super(server, key);
    }

    public void doConnect() throws IOException {
        if(!sc.finishConnect()) {
            return ;
        }

        key.interestOps(SelectionKey.OP_WRITE);
    }
}
