package fr.ramatellier.greed.server.context;

import fr.ramatellier.greed.server.Application;
import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.component.IDListComponent;
import fr.ramatellier.greed.server.frame.model.DisconnectedFrame;
import fr.ramatellier.greed.server.frame.model.LogoutDeniedFrame;
import fr.ramatellier.greed.server.frame.model.LogoutGrantedFrame;
import fr.ramatellier.greed.server.visitor.ParentReceiveVisitor;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.util.Objects;
import java.util.logging.Logger;

public class ServerApplicationContext extends Context {

    public ServerApplicationContext(Application server, SelectionKey key) {
        super(server, key);
        setVisitor(new ParentReceiveVisitor(this));
    }

    public void confirmLogout(IDComponent id, IDListComponent daughters) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(daughters);
        if(server.isRunning()) {
            queuePacket(new LogoutGrantedFrame());
            if(daughters.sizeList() == 0) {
                server.broadcast(new DisconnectedFrame(new IDComponent(server.getAddress()), id), server.getAddress());
                server.deleteAddress(id.getSocket());
            }
            else {
                server.newLogoutRequest(id.getSocket(), daughters.idPacketList().stream().map(IDComponent::getSocket).toList());
            }
        }
        else {
            queuePacket(new LogoutDeniedFrame());
        }
    }

    public void reconnectServer(InetSocketAddress socket, IDListComponent ancestors) {
        Objects.requireNonNull(socket);
        Objects.requireNonNull(ancestors);
        server.receiveReconnect(socket);
        server.updateRouteTable(socket, socket, this);
        for(var id: ancestors.idPacketList()) {
            server.updateRouteTable(id.getSocket(), socket, this);
        }
        if(server.allConnected()) {
            server.broadcast(new DisconnectedFrame(new IDComponent(server.getAddress()), new IDComponent(server.getAddressLogout())), server.getAddress());
            server.deleteAddress(server.getAddressLogout());
            if(server.isShutdown()) {
                server.sendLogout();
            }
        }
    }
}
