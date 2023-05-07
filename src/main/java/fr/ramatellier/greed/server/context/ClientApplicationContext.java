package fr.ramatellier.greed.server.context;

import fr.ramatellier.greed.server.Application;
import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.component.IDListComponent;
import fr.ramatellier.greed.server.frame.model.PleaseReconnectFrame;
import fr.ramatellier.greed.server.visitor.ChildReceiveVisitor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.util.Objects;

public class ClientApplicationContext extends Context {
    public ClientApplicationContext(Application server, SelectionKey key) {
        super(server, key);
        setVisitor(new ChildReceiveVisitor(this));
    }

    public void doConnect() throws IOException {
        if(!sc.finishConnect()) {
            return ;
        }

        key.interestOps(SelectionKey.OP_WRITE);
    }

    public void link(IDComponent addressMother, IDListComponent neighbours) {
        Objects.requireNonNull(addressMother);
        Objects.requireNonNull(neighbours);
        server.updateParentAddress(addressMother.getSocket());
        for(var neighbor: neighbours.idPacketList()) {
            System.out.println("Add neighbor " + neighbor.getSocket() + " to root table");
            server.updateRouteTable(neighbor.getSocket(), addressMother.getSocket(), this);
        }
        server.updateRouteTable(addressMother.getSocket(), addressMother.getSocket(), this);
    }

    public void reconnectDaughters() {
        for(var daughterContext: server.daughtersContext()) {
            daughterContext.queuePacket(new PleaseReconnectFrame(new IDComponent(server.getParentSocketAddress())));
        }
    }

    public void shutdownServer(){
        server.shutdown();
    }

    public void handleLogout(InetSocketAddress socket) {
        Objects.requireNonNull(socket);
        if(server.getAddress().equals(socket)) {
            shutdownServer();
        }
        else {
            server.deleteAddress(socket);
            if(server.isShutdown() && socket.equals(server.getParentSocketAddress())) {
                server.sendLogout();
            }
        }
    }

    public void reconnect(IDComponent id) {
        Objects.requireNonNull(id);
        try {
            server.connectToNewParent(id);
        } catch (IOException e) {
            // Ignore exception
        }
    }
}
