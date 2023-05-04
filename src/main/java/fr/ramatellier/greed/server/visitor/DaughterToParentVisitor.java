package fr.ramatellier.greed.server.visitor;

import fr.ramatellier.greed.server.ClientApplicationContext;
import fr.ramatellier.greed.server.Context;
import fr.ramatellier.greed.server.Server;
import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.model.*;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

public class DaughterToParentVisitor extends FrameVisitor {
    private final ClientApplicationContext context;
    private final Server server;

    private static final Logger logger = Logger.getLogger(DaughterToParentVisitor.class.getName());

    /**
     * @param server The server
     * @param context The context
     */
    public DaughterToParentVisitor(Server server, ClientApplicationContext context){
        Objects.requireNonNull(context);
        Objects.requireNonNull(server);
        this.context = context;
        this.server = server;
    }

    @Override
    public void visit(ConnectKOFrame packet) {
        server.shutdown();
    }

    @Override
    public Server server() {
        return server;
    }

    @Override
    public Context context() {
        return context;
    }

    public void visit(ConnectOKFrame packet){
        System.out.println("ConnectOKFrame");
        context.associate(packet.idMother(), packet.neighbours());
    }


    @Override
    public void visit(LogoutDeniedFrame packet) {
        System.out.println(packet.getClass() + " " + context.getClass().getName());
        System.out.println("LOGOUT DENIED");
    }
    @Override
    public void visit(LogoutGrantedFrame packet) {
        System.out.println(packet.getClass() + " " + context.getClass().getName());
        System.out.println("LOGOUT GRANTED");
        context.reconnectDaughters();
    }

    @Override
    public void visit(PleaseReconnectFrame packet) {
        System.out.println(packet.getClass() + " " + context.getClass().getName());
        try {
            server.connectToNewParent(packet.id());
        } catch (IOException e) {
            // Ignore exception
        }
    }
    @Override
    public void visit(DisconnectedFrame packet) {
        System.out.println(packet.getClass() + " " + context.getClass().getName());
        if(server.getAddress().equals(packet.id().getSocket())) {
            server.shutdown();
        }
        else {
            server.deleteAddress(packet.id().getSocket());

            if(server.isShutdown() && packet.id().getSocket().equals(server.getParentSocketAddress())) {
                server.sendLogout();
            }
        }
    }

}
